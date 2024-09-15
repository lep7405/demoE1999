package com.example.demoe.Controller;

import com.example.demoe.Dto.OrderPaid.OrderPaidDto;
import com.example.demoe.Dto.OrderPaid.OrderPaidItemDto;
import com.example.demoe.Dto.OrderPaid.ProVarDto;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.Order1Item;
import com.example.demoe.Entity.Order.OrderPaid;
import com.example.demoe.Entity.Shipping.Shipping;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Entity.cart.CartItem;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Helper.JedisSingleton;
import com.example.demoe.Repository.*;
import com.example.demoe.Service.PaymentService;
import com.example.demoe.Service.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path2;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/payment")

public class PaymentReponseController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private Order1Repo order1Repo;
    @Autowired
    private ProductVarRepo proVarRepo;
    @Autowired
    private ShippingRepo shippingRepo;
    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private UserRepo userRepo;
    @Autowired
    private OrderPaidRepo orderPaidRepo;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private CartItemRepo cartItemRepo;
    @GetMapping("/vn-pay-callback")
    public String payCallbackHandler(HttpServletRequest request, Model model) throws Exception {
        String status = request.getParameter("vnp_ResponseCode");
        String content = request.getParameter("vnp_OrderInfo");
        String ref = request.getParameter("vnp_TxnRef");
        Order1 order2=order1Repo.findById(Long.parseLong(ref)).get();
        User user2 = userRepo.findById(order2.getUser().getId()).get();
        Long userId = user2.getId();
        String redisKey = "cart:" + userId;

        for(Order1Item order1Item:order1Repo.findById(Long.parseLong(ref)).get().getOrder1ItemList()) {
            Optional<CartItem> cartItem= cartItemRepo.findByIdByProductIdProvarId(order1Item.getProduct().getId(), order1Item.getProVar().getId());
            String pathToQuantity = "$.cartItems[?(@.id==" + cartItem.get().getId() + ")]";

            UnifiedJedis jedis = JedisSingleton.getInstance();
            jedis.jsonDel(redisKey, Path2.of(pathToQuantity));
                cartItem.get().setCart(null);
                cartItemRepo.save(cartItem.get());
                cartItemRepo.delete(cartItem.get());

        }
        if (status.equals("00")) {
            Order1 order1=order1Repo.findById(Long.parseLong(ref)).get();
            order1.setStatus("Paid");
            order1Repo.save(order1);
            Map<Long, List<Order1Item>> groupedItemsByProductId = new HashMap<>();

            for(Order1Item order1Item:order1.getOrder1ItemList()) {
                Long productId = order1Item.getProduct().getId();

                // Nếu chưa có danh sách cho productId này, tạo một danh sách mới
                if (!groupedItemsByProductId.containsKey(productId)) {
                    groupedItemsByProductId.put(productId, new ArrayList<>());
                }

                // Thêm Order1Item vào danh sách tương ứng với productId
                groupedItemsByProductId.get(productId).add(order1Item);
            }

            for (Map.Entry<Long, List<Order1Item>> entry : groupedItemsByProductId.entrySet()) {
                Long productId = entry.getKey();
                List<Order1Item> itemsWithSameProductId = entry.getValue();
//                String order1Json = objectMapper.writeValueAsString(order1.getAddress(),itemsWithSameProductId);
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("address", order1.getAddress());
                orderData.put("items", itemsWithSameProductId);

                String order1Json = objectMapper.writeValueAsString(orderData);

                BigDecimal totalPriceProduct = BigDecimal.valueOf(0);
                for (Order1Item item : itemsWithSameProductId) {
                    ProVar proVar = item.getProVar();
                    proVar.setStock(proVar.getStock() - item.getQuantity());
//                proVar.setSold(proVar.getSold() + order1Item.getQuantity());
                    proVarRepo.save(proVar);
//                    totalPriceProduct = totalPriceProduct.add(proVar.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                Shipping shipping=Shipping.builder()

                        .isPayment(true)
                        .status("Delivery")
                        .propertiesArray(order1Json)
//                        .shippingFee(itemsWithSameProductId.getFirst().getShippingFee())
                        .deliveryTime(LocalDateTime.now())
//                        .priceProduct()
                        .build();
//                shipping.setPriceProduct(totalPriceProduct);
                shippingRepo.save(shipping);
                order1.addShipping(shipping);
                order1Repo.save(order1);
                for (Order1Item item : itemsWithSameProductId) {
                    shipping.addOrder1Item(item);
                }
                shippingRepo.save(shipping);
                User user1 = userRepo.findById(order1.getUser().getId()).get();
                user1.addShipping(shipping);
                userRepo.save(user1);

            }
            transfer(order1.getId(), order1.getUser().getId());
            model.addAttribute("message", "Payment Successful!");
        } else {
            model.addAttribute("message", "Payment Failed!");
            Order1 order1=order1Repo.findById(Long.parseLong(ref)).get();
            transfer(order1.getId(), order1.getUser().getId());
        }
        return "paymentCallback";
    }
    public void transfer( Long order1Id,Long userId) throws JsonProcessingException {
        User user1 = userRepo.findById(userId).get();
        Optional<Order1> order1Opt = order1Repo.findById(order1Id);
        List<OrderPaidItemDto> orderPaidItemDtos = new ArrayList<>();
        order1Opt.get().getOrder1ItemList().forEach(order1Item -> {
            List<ProVarDto> proVarDtos = new ArrayList<>();
            order1Item.getProVar().getVars().forEach(var -> {
                ProVarDto proVarDto=ProVarDto.builder()
                        .id(var.getId())
                        .key1(var.getKey1())
                        .value(var.getValue())
                        .build();
                proVarDtos.add(proVarDto);
            });
            OrderPaidItemDto orderPaidItemDto = OrderPaidItemDto.builder()
                    .proVarDtos(proVarDtos)
                    .productName(order1Item.getProduct().getProductName())
                    .productId(order1Item.getProduct().getId())
                    .quantity(order1Item.getQuantity())
                    .id(order1Item.getId())
                    .productVarId(order1Item.getProVar().getId())
                    .proVarDtos(proVarDtos)
                    .image(s3Service.getPresignedUrl(order1Item.getProVar().getImage()))
                    .provarPrice(order1Item.getProVar().getPrice())
                    .shippingFee(order1Item.getShippingFee())
                    .build();
            orderPaidItemDtos.add(orderPaidItemDto);

        });
        OrderPaidDto orderPaidDto = OrderPaidDto.builder()
                .id(order1Id)
                .order1Date(order1Opt.get().getOrder1Date())
                .orderPaidItemDtos(orderPaidItemDtos)
                .txnRep(order1Opt.get().getTxnRep())
                .price(order1Opt.get().getPrice())
                .status(order1Opt.get().getStatus())
                .build();

        if (order1Opt.isPresent()) {
            // Chuyển đổi đối tượng Order1 sang JSON
            String order1Json = objectMapper.writeValueAsString(orderPaidDto);
//            Map<String, Object> order1Map = objectMapper.convertValue(order1Opt.get());
//
//            Map<String, Object> jsonMap = new HashMap<>();
//            jsonMap.put(String.valueOf(order1Opt.get().getId()), order1Json);
//            // Tạo một bản ghi mới trong Order2
            OrderPaid order2 = new OrderPaid();
            order2.setPropertiesArray(order1Json);
            order2.setCreatedAt(LocalDateTime.now());
            orderPaidRepo.save(order2);
            user1.addOrderPaid(order2);

            userRepo.save(user1);
        }
    }
}






//   for(Order1Item order1Item:order1.getOrder1ItemList()) {
//ProVar proVar = order1Item.getProVar();
//                System.out.println("Provar Price:"+proVar.getPrice());
//        System.out.println("OrderItem Quantity:"+order1Item.getQuantity());
//        proVar.setStock(proVar.getStock() - order1Item.getQuantity());
////                proVar.setSold(proVar.getSold() + order1Item.getQuantity());
//        proVarRepo.save(proVar);
//String order1Json = objectMapper.writeValueAsString(order1.getAddress());
//Shipping shipping=Shipping.builder()
//
//        .isPayment(true)
//        .status("Delivery")
//        .propertiesArray(order1Json)
//        .shippingFee(order1Item.getShippingFee())
//        .deliveryTime(LocalDateTime.now())
////                        .priceProduct()
//        .build();
//                shipping.setPriceProduct(proVar.getPrice().multiply(BigDecimal.valueOf(order1Item.getQuantity())));
//        shippingRepo.save(shipping);
//                order1.addShipping(shipping);
//                order1Repo.save(order1);
//                shipping.addOrder1Item(order1Item);
//
//                shippingRepo.save(shipping);
//User user1 = userRepo.findById(order1.getUser().getId()).get();
//                user1.addShipping(shipping);
//                userRepo.save(user1);
//transfer(order1.getId(), order1.getUser().getId());
//        }