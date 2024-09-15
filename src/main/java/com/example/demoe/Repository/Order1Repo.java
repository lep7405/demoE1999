package com.example.demoe.Repository;

import com.example.demoe.Entity.Order.Order1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Order1Repo extends JpaRepository<Order1,Long> {
    @Query("SELECT p FROM Order1 p WHERE p.user.id = :userId")
    List<Order1> findAllByUserId(@Param("userId") Long userId);
}
