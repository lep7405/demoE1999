package com.example.demoe.Dto.Discount;

import com.example.demoe.Entity.product.Discount;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ListDiscountDtoMessage {
    private List<Discount> discounts;
    private String message;
    public ListDiscountDtoMessage(List<Discount> discounts, String message) {
        this.discounts = discounts;
        this.message = message;
    }
    public ListDiscountDtoMessage(String message) {
        this.message = message;
    }
}
