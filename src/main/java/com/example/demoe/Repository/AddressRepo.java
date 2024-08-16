package com.example.demoe.Repository;

import com.example.demoe.Entity.Address;

import org.springframework.data.jpa.repository.JpaRepository;



public interface AddressRepo extends JpaRepository<Address,Long> {

}
