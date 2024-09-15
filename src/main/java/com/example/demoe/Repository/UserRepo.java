package com.example.demoe.Repository;

import com.example.demoe.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

//        @Query("SELECT u FROM User u WHERE u.order1s.id = :userId")
//    Optional<User> findByUserId(@Param("userId") Long userId);

}
