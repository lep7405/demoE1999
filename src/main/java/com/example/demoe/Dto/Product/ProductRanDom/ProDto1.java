package com.example.demoe.Dto.Product.ProductRanDom;

import com.example.demoe.Entity.product.Discount;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter

public class ProDto1 {
    private Long id;
    private String productName;
    private String image;
    private BigDecimal price;
    private Double averageStars;
    private Integer numberOfStars;
    private Discount discount;
}
