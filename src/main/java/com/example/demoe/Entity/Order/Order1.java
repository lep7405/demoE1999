package com.example.demoe.Entity.Order;

import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.Shipping.Shipping;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.CartItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order1 {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long txnRep;
    private LocalDate order1Date;
    private String status;

    private BigDecimal price;
    @OneToMany(mappedBy = "order1")
    private List<Order1Item> order1ItemList;
    public void addOrder1Item(Order1Item order1Item) {
        if(order1ItemList == null) {
            order1ItemList = new ArrayList<>();
        }
        order1ItemList.add(order1Item);
        order1Item.setOrder1(this);
    }

    @OneToOne(mappedBy = "order1")
    private Address address;

    public void addAddress(Address address) {
        this.address = address;
        address.setOrder1(this);
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "order1")
    private List<Shipping> shippingList;

    public void addShipping(Shipping shipping) {
        if(shippingList == null) {
            shippingList = new ArrayList<>();
        }
        shippingList.add(shipping);
        shipping.setOrder1(this);
    }
}
