package com.example.demoe.Controller.Discount.Request;

import com.example.demoe.Entity.product.Discount;
import lombok.Getter;

@Getter
public class CreatepPerDiscountRequest {
    private Discount discount;
    private Long id;
}
