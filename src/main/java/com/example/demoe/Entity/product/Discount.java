package com.example.demoe.Entity.product;

import com.example.demoe.Entity.Admin;
import com.example.demoe.Entity.cart.CartItem;
import com.example.demoe.Entity.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String type;
    private Integer discountValue;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Integer level;

    @ManyToMany(mappedBy = "discounts")
    @JsonIgnore
    private List<Product> productList;

    @ManyToOne()
    @JoinColumn(name="admin_id")
    @JsonIgnore
    private Admin admin;

    @OneToOne
    @JoinColumn(name = "cartItem_id")
    @JsonIgnore
    private CartItem cartItem;
}
