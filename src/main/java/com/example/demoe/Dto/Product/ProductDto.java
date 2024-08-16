package com.example.demoe.Dto.Product;

import com.example.demoe.Entity.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ProductDto {
    private Product product;
    private String message;
    public ProductDto(Product product, String message) {
        this.product = product;
        this.message = message;
    }
    public ProductDto(String message){
        this.message = message;
    }
}
