package com.example.demoe.Service;

import com.example.demoe.Dto.Cart.CartItemDto;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.Order1Item;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Entity.product.Product;
import com.example.demoe.Repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@Service

@AllArgsConstructor
public class OrderService {
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

    public ResponseEntity<String> createOrder1(List<CartItemDto> cartItemDtoList) {
        Order1 order1=Order1.builder().build();

        order1Repo.save(order1);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        user1.get().addOrder1(order1);
        userRepo.save(user1.get());
        for (CartItemDto cartItemDto : cartItemDtoList) {
            System.out.println(cartItemDto.getId());
            Product product=productRepo.findById(cartItemDto.getId()).get();
            ProVar productVar=productVarRepo.findById(cartItemDto.getProvarId()).get();
            Order1Item order1Item=Order1Item.builder()
                    .quantity((short) cartItemDto.getQuantity())
                    .build();
            order1ItemRepo.save(order1Item);
            product.addOrder1Item(order1Item);
            productRepo.save(product);
            order1Item.addPro(productVar);
            order1ItemRepo.save(order1Item);
            order1.addOrder1Item(order1Item);
            order1Repo.save(order1);

        }
        return ResponseEntity.ok("Order created successfully");
    }
}