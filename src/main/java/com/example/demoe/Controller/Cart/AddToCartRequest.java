package com.example.demoe.Controller.Cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {

    private Long id;
    private Long provarId;
    private int quantity;
}
