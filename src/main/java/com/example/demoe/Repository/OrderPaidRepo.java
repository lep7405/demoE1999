package com.example.demoe.Repository;

import com.example.demoe.Entity.Order.OrderPaid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderPaidRepo extends JpaRepository<OrderPaid, Long> {
    @Query("SELECT p FROM OrderPaid p WHERE p.user.id = :userId")
    List<OrderPaid> findAllByUserId(@Param("userId") Long userId);
}
