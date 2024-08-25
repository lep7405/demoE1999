package com.example.demoe.Entity.Order;

import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.User;
import com.example.demoe.Entity.cart.CartItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
    private String price;
    private String status;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
