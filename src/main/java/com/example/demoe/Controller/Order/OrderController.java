package com.example.demoe.Controller.Order;

import com.example.demoe.Dto.Cart.CartItemDto;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.Order1Item;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Entity.product.Product;
import com.example.demoe.Repository.*;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    @CrossOrigin(origins = "http://localhost:5173")

    @PostMapping("/createOrder1")
    public ResponseEntity<Order1> createOrder1(@RequestBody List<CartItemDto> cartItemDtoList) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        Order1 order1=Order1.builder()
                .status("Pending payment")

                .build();

        order1Repo.save(order1);
        order1.setTxnRep(order1.getId());
        order1Repo.save(order1);
        user1.get().addOrder1(order1);
        userRepo.save(user1.get());
        for (CartItemDto cartItemDto : cartItemDtoList) {
            Product product=productRepo.findById(cartItemDto.getProductId()).get();
//            Hibernate.initialize(product.getOrder1Items());
            ProVar productVar=productVarRepo.findById(cartItemDto.getProvarId()).get();
            Order1Item order1Item=Order1Item.builder()
                    .quantity((short) cartItemDto.getQuantity())
                    .build();
            order1ItemRepo.save(order1Item);
            product.addOrder1Item(order1Item);
            order1Item.addPro(productVar);
            order1.addOrder1Item(order1Item);
        }
        order1Repo.save(order1);
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
}
