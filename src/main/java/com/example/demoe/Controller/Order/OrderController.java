package com.example.demoe.Controller.Order;

import com.example.demoe.Dto.Cart.CartItemDto;
import com.example.demoe.Dto.OrderPaid.OrderDetailDto;
import com.example.demoe.Dto.OrderPaid.OrderPaidDto;
import com.example.demoe.Dto.OrderPaid.OrderPaidItemDto;
import com.example.demoe.Dto.OrderPaid.ProVarDto;
import com.example.demoe.Dto.Shipping.ShippingDto;
import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.Order1Item;
import com.example.demoe.Entity.Order.OrderPaid;
import com.example.demoe.Entity.Shipping.Shipping;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Entity.product.Product;
import com.example.demoe.Repository.*;
import com.example.demoe.Service.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/order")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class OrderController {
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductVarRepo productVarRepo;
    @Autowired
    private Order1Repo order1Repo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private Order1ItemRepo order1ItemRepo;
    @Autowired
    private AddressRepo addressRepo;
    @Autowired
    private OrderPaidRepo orderPaidRepo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private ShippingRepo shippingRepo;
    @Autowired
    private DiscountRepo discountRepo;

    @CrossOrigin(origins = "http://localhost:5173")

    @PostMapping("/createOrder1")
    public ResponseEntity<Order1> createOrder1(@RequestBody createOrder1Request createOrder1Request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<User> userOpt = userRepo.findByEmail(currentUser.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = userOpt.get();
        List<CartItemDto> cartItems = createOrder1Request.getCartItemDtoList();
        Address address = createOrder1Request.getAddress();
        Order1 order1 = Order1.builder()
                .status("Pending payment")
                .build();
        order1 = order1Repo.save(order1);
        order1.setTxnRep(order1.getId());
        order1 = order1Repo.save(order1);
        user.addOrder1(order1);
        userRepo.save(user);
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> processedProductIds = new HashSet<>();

        for (CartItemDto cartItemDto : cartItems) {
            Product product = productRepo.findById(cartItemDto.getProductId()).orElseThrow();
            ProVar productVar = productVarRepo.findById(cartItemDto.getProvarId()).orElseThrow();
            boolean isFirstInstance = processedProductIds.add(cartItemDto.getProductId());

            Order1Item order1Item = Order1Item.builder()
                    .quantity((short) cartItemDto.getQuantity())
                    .shippingFee(isFirstInstance ? BigDecimal.valueOf(15000) : BigDecimal.ZERO)
                    .build();


            order1ItemRepo.save(order1Item);
            product.addOrder1Item(order1Item);
            productVar.addOrder1Item(order1Item);
            order1.addOrder1Item(order1Item);

            List<Discount> discountList = discountRepo.findDiscounts1((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), product.getId());
            Optional<Discount> discountlv2 = discountList.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 2 && d.getIsActive() == true)
                    .findFirst();
            Optional<Discount> discountlv1 = discountList.stream()
                    .filter(d -> d.getLevel() != null && d.getLevel() == 1 && d.getIsActive() == true)
                    .findFirst();
            if (discountlv2.isPresent()) {
                BigDecimal discountPercentage = discountlv2.get().getDiscountValue();
                BigDecimal itemTotal = cartItemDto.getPrice().multiply(BigDecimal.valueOf(cartItemDto.getQuantity())).multiply(discountPercentage).divide(BigDecimal.valueOf(100));
                total = total.add(itemTotal).add(order1Item.getShippingFee());

            } else {
                if (discountlv1.isPresent()) {
                    BigDecimal discountPercentage = discountlv1.get().getDiscountValue();
                    BigDecimal itemTotal = cartItemDto.getPrice().multiply(BigDecimal.valueOf(cartItemDto.getQuantity())).multiply(discountPercentage).divide(BigDecimal.valueOf(100));
                    total = total.add(itemTotal).add(order1Item.getShippingFee());
                } else {
                    total = total.add(cartItemDto.getPrice().multiply(BigDecimal.valueOf(cartItemDto.getQuantity()))).add(order1Item.getShippingFee());
                }
            }
        }
        Address savedAddress = addressRepo.findById(address.getId()).orElseThrow();
        order1.addAddress(savedAddress);
        order1.setOrder1Date(LocalDate.now());
        order1.setPrice(total);
        order1 = order1Repo.save(order1);
        return ResponseEntity.ok(order1);
    }

    @GetMapping("/getOrder1Item/{id}")
    public ResponseEntity<Order1Item> getOrder1Item(@PathVariable("id") Long id) {
        Optional<Order1Item> order1 = order1ItemRepo.findById(id);
        return ResponseEntity.ok(order1.get());
    }

    @GetMapping("/getOrder1/{id}")
    public ResponseEntity<Order1> getOrder1(@PathVariable("id") Long id) {
        Optional<Order1> order1 = order1Repo.findById(id);
        return ResponseEntity.ok(order1.get());
    }

    @GetMapping("/getAllOrder1")
    public ResponseEntity<List<Order1>> getAllOrder1() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);

        List<Order1> order1 = order1Repo.findAllByUserId(user1.get().getId());
        return ResponseEntity.ok(order1);
    }

    @PostMapping("/transfer/{order1Id}")
    public ResponseEntity<OrderPaid> transfer(@PathVariable("order1Id") Long order1Id) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        Optional<Order1> order1Opt = order1Repo.findById(order1Id);
        List<OrderPaidItemDto> orderPaidItemDtos = new ArrayList<>();
        order1Opt.get().getOrder1ItemList().forEach(order1Item -> {
            List<ProVarDto> proVarDtos = new ArrayList<>();
            order1Item.getProVar().getVars().forEach(var -> {
                ProVarDto proVarDto = ProVarDto.builder()
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
            OrderPaid order2 = new OrderPaid();
            order2.setPropertiesArray(order1Json);
            order2.setCreatedAt(LocalDateTime.now());
            orderPaidRepo.save(order2);
            user1.get().addOrderPaid(order2);

            userRepo.save(user1.get());
            return ResponseEntity.ok(order2);
        } else {
            throw new Exception("Order1 not found");
        }
    }

    @GetMapping("/getAllOrderPaid")
    public ResponseEntity<List<?>> getAllOrderPaid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        List<OrderPaid> orderPaid = orderPaidRepo.findAllByUserId(user1.get().getId());
        List<?> orderPaidList = objectMapper.convertValue(orderPaid, List.class);
        return ResponseEntity.ok(orderPaidList);
    }

    @GetMapping("/getOrderDetail/{order1Id}/{productId}")
    public ResponseEntity<ShippingDto> getOrderDetail(@PathVariable("productId") Long productId, @PathVariable("order1Id") Long order1Id) throws JsonProcessingException {

        Order1 order1 = order1Repo.findById(order1Id).get();
        List<Shipping> shipping = shippingRepo.findByOrderPaidId(order1Id);
        for (Shipping s : shipping) {
            String jsonString = objectMapper.writeValueAsString(s.getPropertiesArray());

            Map<String, Object> orderData = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) orderData.get("items");
            Address address = objectMapper.convertValue(orderData.get("address"), Address.class);
            List<Order1Item> order1ItemList = objectMapper.convertValue(
                    itemsList,
                    new TypeReference<List<Order1Item>>() {
                    }
            );

            for (Order1Item order1Item : order1ItemList) {
                System.out.println("productId" + order1Item.getProduct().getId());
                if (order1Item.getProduct().getId() == productId) {
                    ShippingDto shippingDto = ShippingDto.builder()
                            .id(s.getId())
                            .deliveryTime(s.getDeliveryTime())
                            .status(s.getStatus())
                            .orderStatus(s.getOrder1().getStatus())
                            .totalProductPrice(order1.getPrice())
                            .build();
                    List<OrderPaidItemDto> orderPaidItemDtos = new ArrayList<>();
                    for (Order1Item order1Item1 : s.getOrder1ItemList()) {
                        OrderPaidItemDto orderPaidItemDto = OrderPaidItemDto
                                .builder()
                                .productVarId(order1Item1.getProVar().getId())
                                .productId(order1Item1.getProduct().getId())
                                .image(s3Service.getPresignedUrl(order1Item1.getProVar().getImage()))
                                .shippingFee(order1Item1.getShippingFee())
                                .productName(order1Item1.getProduct().getProductName())
                                .quantity(order1Item1.getQuantity())
                                .provarPrice(order1Item1.getProVar().getPrice())
                                .id(order1Item1.getId())
                                .proVarDtos(objectMapper.convertValue(order1Item1.getProVar().getVars(), List.class))
                                .build();
                        orderPaidItemDtos.add(orderPaidItemDto);
                    }
                    shippingDto.setOrderPaidItemDtos(orderPaidItemDtos);
                    shippingDto.setAddress(address);
                    OrderDetailResponse orderDetailResponse = OrderDetailResponse.builder()
                            .shipping(s)
                            .build();
                    return ResponseEntity.ok(shippingDto);
                }
            }
        }
        return ResponseEntity.notFound().build();

    }
}