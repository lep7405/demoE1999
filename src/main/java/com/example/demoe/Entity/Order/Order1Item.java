package com.example.demoe.Entity.Order;

import com.example.demoe.Entity.Shipping.Shipping;
import com.example.demoe.Entity.product.ProVar;
import com.example.demoe.Entity.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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
    private BigDecimal shippingFee;
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne()
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order1 order1;

    @ManyToOne
    @JoinColumn(name = "pro_var_id")
    private ProVar proVar;

    @ManyToOne
    @JoinColumn(name = "shipping_id")
    @JsonIgnore
    private Shipping shipping;

}
