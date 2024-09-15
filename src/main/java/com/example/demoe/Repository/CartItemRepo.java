package com.example.demoe.Repository;

import com.example.demoe.Entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepo extends JpaRepository<CartItem,Long> {
    @Query("select c from CartItem c where c.productId=:productId and c.proVar.id=:provarId")
    Optional<CartItem> findByIdByProductIdProvarId(@Param("productId") Long productId, @Param("provarId") Long provarId);
}
