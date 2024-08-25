package com.example.demoe.Entity.cart;

import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.ProVar;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long productId;
    private Short quantity;
    private BigDecimal discountValue;
    private String productName;
    private Integer max1Buy;
//    private BigDecimal price;

    @OneToOne
    @JoinColumn(name = "pro_var_id")
    private ProVar proVar;

    public void addPro(ProVar proVar) {
        this.proVar = proVar;
        proVar.setCartItem(this);
    }

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;


//    @OneToOne(mappedBy = "cartItem")
//    private Discount discount;
//
//    public void addDiscount(Discount discount) {
//        this.discount = discount;
//        discount.setCartItem(this);
//    }
}
