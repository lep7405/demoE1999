package com.example.demoe.Controller.Shipping;

import com.example.demoe.Dto.OrderPaid.OrderPaidItemDto;
import com.example.demoe.Dto.Shipping.ShippingDto;
import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.Order.Order1Item;
import com.example.demoe.Entity.Shipping.Shipping;
import com.example.demoe.Entity.User;
import com.example.demoe.Repository.ShippingRepo;
import com.example.demoe.Repository.UserRepo;
import com.example.demoe.Service.S3Service;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/shipping")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class ShippingController {
    @Autowired
    private ShippingRepo shippingRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/getAllShipping")
    public ResponseEntity<List<Shipping>> getAllShipping() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        List<Shipping> shippings = shippingRepo.findAll();
        return ResponseEntity.ok(shippings);
    }

    @GetMapping("/getAllShipping2")
    public ResponseEntity<List<?>> getAllOrderPaid(@RequestParam("texts") String texts) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        List<Shipping> shippings = shippingRepo.findByUserId(user1.get().getId());
        List<ShippingDto> shippingDtoList=new ArrayList<>();
        for(Shipping shipping:shippings){
            if(shipping.getStatus().equals(texts)){
                ShippingDto shippingDto = ShippingDto.builder()
                        .id(shipping.getId())
                        .deliveryTime(shipping.getDeliveryTime())
                        .status(shipping.getStatus())
                        .orderStatus(shipping.getOrder1().getStatus())
                        .build();
                List<OrderPaidItemDto> orderPaidItemDtos = new ArrayList<>();

                String jsonString = objectMapper.writeValueAsString(shipping.getPropertiesArray());
                System.out.println("propertiesArrayJsons"+jsonString);
                Map<String, Object> orderData = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
//            List<Order1Item> order1ItemList = (List<Order1Item>) orderData.get("items");
                List<Map<String, Object>> itemsList = (List<Map<String, Object>>) orderData.get("items");
                Address address = objectMapper.convertValue(orderData.get("address"), Address.class);
                shippingDto.setAddress(address);

                List<Order1Item> order1ItemList = objectMapper.convertValue(
                        itemsList,
                        new TypeReference<List<Order1Item>>() {}
                );
                for(Order1Item order1Item:order1ItemList){
                    OrderPaidItemDto orderPaidItemDto=OrderPaidItemDto
                            .builder()
                            .productVarId(order1Item.getProVar().getId())
                            .productId(order1Item.getProduct().getId())
                            .image(s3Service.getPresignedUrl(order1Item.getProVar().getImage()))
                            .shippingFee(order1Item.getShippingFee())
                            .productName(order1Item.getProduct().getProductName())
                            .quantity(order1Item.getQuantity())
                            .provarPrice(order1Item.getProVar().getPrice())
                            .id(order1Item.getId())
                            .proVarDtos(objectMapper.convertValue(order1Item.getProVar().getVars(),List.class))
                            .build();
                    orderPaidItemDtos.add(orderPaidItemDto);
                }
                shippingDto.setOrderPaidItemDtos(orderPaidItemDtos);
                shippingDtoList.add(shippingDto);
            }

        }
        return ResponseEntity.ok(shippingDtoList);
    }

    @GetMapping("/getShippingById/{id}")
    public ResponseEntity<Shipping> getShippingById(@PathVariable("id") Long id) {
        Optional<Shipping> shipping = shippingRepo.findById(id);
        return ResponseEntity.ok(shipping.get());
    }
}
