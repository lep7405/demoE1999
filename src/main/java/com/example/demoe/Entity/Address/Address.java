package com.example.demoe.Entity.Address;

import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nameAddress;
    private Boolean isDefault;
    private String phone;
    private AddressType addressType;
    private String province;
    private String district;
    private String ward;
    private String fullName;
    @ManyToOne
    @JoinColumn(name="user_id")
    @JsonIgnore
    private User user;

    @OneToOne()
    @JoinColumn(name = "order1_id")
    @JsonIgnore
    private Order1 order1;
}
