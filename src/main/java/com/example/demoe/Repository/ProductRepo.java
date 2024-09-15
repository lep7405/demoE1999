package com.example.demoe.Repository;

import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.productName = :productName")
    Optional<Product> findByProductName(String productName);
    Page<Product> findAll(Pageable pageable);
    @Query("SELECT p FROM Product p JOIN p.discounts d WHERE d.id = :discountId")
    List<Product> findProductByDiscountId(@Param("discountId") Long discountId);

    @Query("SELECT p FROM Product p " +
            "JOIN p.countProduct cp " +
            "JOIN cp.countPerMonths cpm " +
            "WHERE cpm.dateCount = :date " +

            "GROUP BY p " +
            "ORDER BY cpm.countPro DESC")
    Page<Product> findTop10ProductsWithHighestCountPerMonth( @Param("date") YearMonth date, Pageable pageable);


    @Query("SELECT p FROM Product p JOIN p.discounts d WHERE d.startDate <= :startDate "
            + "AND d.endDate >= :startDate "
            + "AND d.isActive = true"
    )
    List<Product> findDiscounts1(@Param("startDate") LocalDateTime startDate, Pageable pageable);
}
