package com.example.demoe.Repository;

import com.example.demoe.Entity.product.ProVar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVarRepo extends JpaRepository<ProVar,Long> {
}
