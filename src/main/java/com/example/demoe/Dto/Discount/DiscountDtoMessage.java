package com.example.demoe.Dto.Discount;

import com.example.demoe.Entity.product.Discount;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountDtoMessage {
    private String message;
    private Discount discount;
    private Long id;
    public DiscountDtoMessage(String message, Discount discount,Long id) {
        this.message = message;
        this.discount = discount;
        this.id=id;
    }
    public DiscountDtoMessage(String message, Discount discount) {
        this.message = message;
        this.discount = discount;

    }
    public DiscountDtoMessage(String message,Long id) {
        this.message = message;
        this.id=id;
    }
    public DiscountDtoMessage(String message) {
        this.message = message;
    }
}
