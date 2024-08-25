package com.example.demoe.Repository;

import com.example.demoe.Entity.Shipping.Shipping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingRepo extends JpaRepository<Shipping,Long> {
}
