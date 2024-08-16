package com.example.demoe.Repository;

import com.example.demoe.Entity.product.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface DiscountRepo extends JpaRepository<Discount,Long> {
    @Query("SELECT d FROM Discount d JOIN d.productList p WHERE p.id = :productId AND d.isActive = :isActive AND "
            + "((d.startDate <= :startDate AND d.endDate >= :startDate) OR "
            + "(d.startDate <= :endDate AND d.endDate >= :endDate))")

    List<Discount> findDiscountsByProductAndDateRangeAndIsActive(@Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate,
                                                                 @Param("isActive") boolean isActive,
                                                                 @Param("productId") Long productId);

    @Query("SELECT d FROM Discount d  WHERE d.level=2 AND d.startDate <= :startDate "
            + "AND d.endDate >= :endDate "
            + "AND d.isActive = :isActive")
    List<Discount> findDiscounts(@Param("startDate") LocalDate startDate,
                                                                 @Param("endDate") LocalDate endDate,
                                                                 @Param("isActive") boolean isActive
                                                                );
    //getAllProduct
    @Query("SELECT d FROM Discount d JOIN d.productList p WHERE p.id = :productId AND d.startDate <= :startDate "
            + "AND d.endDate >= :startDate "
            + "AND d.isActive = true"
    )
    List<Discount> findDiscounts1(@Param("startDate") LocalDate startDate, @Param("productId") Long productId);

    @Query("SELECT d FROM Discount d WHERE d.level = :level "

    )
    List<Discount> findAllByLevel(@Param("level") Integer level);

    @Query("SELECT d FROM Discount d JOIN d.productList p WHERE p.id=:id ")
    List<Discount> findAllBy1Product(@Param("id") Long id);
}
