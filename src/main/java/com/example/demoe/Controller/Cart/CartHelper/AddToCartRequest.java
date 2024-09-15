package com.example.demoe.Controller.Cart.CartHelper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddToCartRequest {

    private Long id;
    private Long provarId;
    private int quantity;
}
