package com.example.demoe.Entity.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CountProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @OneToOne
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @OneToMany(mappedBy = "countProduct")
    private List<CountPerMonth> countPerMonths;

    public void addCountPerMonth(CountPerMonth countPerMonth) {
        if(countPerMonths == null) {
            countPerMonths=new ArrayList<>();
        }
        countPerMonths.add(countPerMonth);
        countPerMonth.setCountProduct(this);
    }
}
