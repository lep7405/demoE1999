package com.example.demoe.Dto.Cart;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class CartDto {
    private Long id;
    private List<CartItemDto> cartItems;

    public CartDto(Long id,  List<CartItemDto> cartItems){
        this.id=id;
        this.cartItems=cartItems;
    }
}
