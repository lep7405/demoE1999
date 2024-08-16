package com.example.demoe.Repository;

import com.example.demoe.Entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepo extends JpaRepository<Cart,Long> {
}
