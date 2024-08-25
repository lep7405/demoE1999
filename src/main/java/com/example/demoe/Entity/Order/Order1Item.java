package com.example.demoe.Entity.Order;

import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Entity.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order1Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Short quantity;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne()
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order1 order1;

    @OneToOne
    @JoinColumn(name = "pro_var_id")
    private ProVar proVar;

    public void addPro(ProVar proVar) {
        this.proVar = proVar;
        proVar.setOrder1Item(this);
    }
}
