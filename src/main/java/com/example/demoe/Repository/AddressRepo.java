package com.example.demoe.Repository;

import com.example.demoe.Entity.Address.Address;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface AddressRepo extends JpaRepository<Address,Long> {
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId")
    List<Address> findListAddressByUser(@Param("userId") Long userId);
}
