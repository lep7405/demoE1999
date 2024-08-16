package com.example.demoe.Repository;

import com.example.demoe.Entity.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category,Long> {
}
