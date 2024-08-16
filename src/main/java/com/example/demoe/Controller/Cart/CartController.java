package com.example.demoe.Controller.Cart;

import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private CartItemRepo cartItemRepo;
    @Autowired
    private ProductVarRepo productVarRepo;
    @Autowired
    private UserRepo userRepo;

    @GetMapping("/getCart")
    public ResponseEntity<Cart> getCart(){
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String email = user.getEmail();
        Optional<User> user1 = userRepo.findByEmail(email);
        if (!user1.isPresent()) {
            return null;
        }
        Cart cart=user1.get().getCart();
        return ResponseEntity.ok(cart);
    }
}
//@RequestParam("id") Long productId,@RequestParam("provarId") Long provarId,@RequestParam("quantity") int quantity