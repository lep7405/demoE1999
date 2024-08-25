package com.example.demoe.Repository;

import com.example.demoe.Entity.Order.Order1Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Order1ItemRepo extends JpaRepository<Order1Item, Long> {
}
