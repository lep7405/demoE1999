package com.example.demoe.Entity.product;

import com.example.demoe.Entity.cart.CartItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProVar {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private BigDecimal price;
    private BigDecimal stock;
    @Column(columnDefinition = "TEXT")
    private String image;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @OneToMany(mappedBy = "proVar")
    private List<Var> vars;

    public void addVar(Var var) {
        if (vars == null) {
            vars = new ArrayList<>();
        }
        vars.add(var);
        var.setProVar(this);
    }

    @OneToOne
    @JoinColumn(name = "cartItem_id")
    @JsonIgnore
    private CartItem cartItem;
}
