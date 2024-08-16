package com.example.demoe.Dto.Product;

import com.example.demoe.Entity.product.Discount;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class ProductDetailDto {
    private Long id;
    private String productName;
    private String brand;
    private Boolean active;
    private String image;
    private BigDecimal price;
    private List<Discount> discountList;
    private Integer totalStock;
    private Short totalVariant;
}
