package com.example.demoe.Controller.Cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {

    private Long id;
    private short quantity;
}
