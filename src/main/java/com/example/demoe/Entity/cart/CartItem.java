package com.example.demoe.Entity.cart;

import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.ProVar;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Short quantity;
    private Integer max1Buy;

    @OneToOne(mappedBy = "cartItem")
    private ProVar proVar;

    public void addPro(ProVar proVar) {
        this.proVar = proVar;
        proVar.setCartItem(this);
    }

    @OneToOne(mappedBy = "cartItem")
    private Discount discount;

    public void addDiscount(Discount discount) {
        this.discount = discount;
        discount.setCartItem(this);
    }
}
