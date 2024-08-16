package com.example.demoe.Entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.YearMonth;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountPerMonth {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long countPro;
    private YearMonth dateCount;
    @OneToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @ManyToOne
    @JoinColumn(name = "countProduct_id")
    @JsonIgnore
    private CountProduct countProduct;
}
