package com.example.demoe.Entity;

import com.example.demoe.Entity.Address.Address;
import com.example.demoe.Entity.Order.Order1;
import com.example.demoe.Entity.Order.OrderPaid;
import com.example.demoe.Entity.Shipping.Shipping;
import com.example.demoe.Entity.cart.Cart;
import com.example.demoe.Entity.product.Review;
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

    @OneToOne(mappedBy = "user")

    private Cart cart;

    @OneToMany(mappedBy = "user")
    private List<Order1> order1s;

    public void addOrder1(Order1 order1) {
        if(order1s == null) {
            order1s = new ArrayList<>();
        }
        order1s.add(order1);
        order1.setUser(this);
    }

    @OneToMany(mappedBy = "user")

    private List<OrderPaid> orderPaidList;

    public void addOrderPaid(OrderPaid orderPaid) {
       if(orderPaidList == null) {
           orderPaidList = new ArrayList<>();
       }
        orderPaidList.add(orderPaid);
        orderPaid.setUser(this);
    }

    @OneToMany(mappedBy = "user")
    private List<Shipping> shippingList;

    public void addShipping(Shipping shipping) {
        if(shippingList == null) {
            shippingList = new ArrayList<>();
        }
        shippingList.add(shipping);
        shipping.setUser(this);
    }

    @OneToMany(mappedBy = "user")
    private List<Review> commentList;;

    public void addComment(Review comment) {
        if(commentList == null) {
            commentList = new ArrayList<>();
        }
        commentList.add(comment);
        comment.setUser(this);
    }
}
