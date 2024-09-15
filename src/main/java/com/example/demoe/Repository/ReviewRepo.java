package com.example.demoe.Repository;

import com.example.demoe.Entity.product.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepo extends JpaRepository<Review, Long> {
    @Query("select c from Review c where c.product.id = :productId and c.user=:userId")
    List<Review> getListCommentByProductId(Long productId, Long userId);

    @Query("select c from Review c where c.user.id = :userId")
    List<Review> getListCommentByUserId(Long userId);
}
