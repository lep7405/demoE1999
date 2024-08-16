package com.example.demoe.Repository;

import com.example.demoe.Entity.product.CountPerMonth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountPerMonthRepo extends JpaRepository<CountPerMonth, Long> {
}
