package com.example.demoe.Entity.product;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Rate {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Integer rateNumber;


    @ManyToOne
    @JoinColumn(name="product_id")
    private Product product;
}
