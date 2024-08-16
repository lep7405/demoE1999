package com.example.demoe.Entity;

import com.example.demoe.Entity.cart.Cart;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class User extends Account {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
    private String phone;

    @OneToMany
    private List<Address> addressList;

    public void addAddress(Address address) {
        if(addressList == null) {
            addressList = new ArrayList<>();
        }
        addressList.add(address);
        address.setUser(this);
    }

    @OneToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}
