package com.example.demoe.Repository;

import com.example.demoe.Entity.Shipping.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShippingRepo extends JpaRepository<Shipping,Long> {
    @Query("SELECT s FROM Shipping s WHERE s.user.id = :userId")
    List<Shipping> findByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM Shipping s WHERE s.order1.id = :orderPaidId")
    List<Shipping> findByOrderPaidId(@Param("orderPaidId") Long orderPaidId);
    @Query("SELECT s FROM Shipping s WHERE s.order1.id = :orderId")
    Optional<Shipping> findByOrderId(@Param("orderId") Long orderId);
}
