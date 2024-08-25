package com.example.demoe.Controller.Cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteResponse {
    private String message;
    private Long id;

    public DeleteResponse(String message, Long id) {
        this.message = message;
        this.id = id;
    }
}
