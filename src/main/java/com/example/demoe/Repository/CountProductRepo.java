package com.example.demoe.Repository;

import com.example.demoe.Entity.product.CountProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountProductRepo extends JpaRepository<CountProduct,Long> {
}
