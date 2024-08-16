package com.example.demoe.Controller.Discount.Request;

import com.example.demoe.Entity.product.Discount;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDiscountPerProRequest {
    private Discount discount;
    private Long productId;
}
