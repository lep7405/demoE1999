package com.example.demoe.Dto.Cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDtoMessage {
    private String message;
    private CartDto cartDto;

    public CartDtoMessage(String message, CartDto cartDto) {
        this.message = message;
        this.cartDto = cartDto;
    }
    public CartDtoMessage(String message) {
        this.message = message;
    }

}
