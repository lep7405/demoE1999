package com.example.demoe.Controller.ProductController.UpdateProductRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraValueUpdate {
    private Long id;
    private String key1;
    private String value;

    // Getters and Setters

    @Override
    public String toString() {
        return "ExtraValue{" +
                "key='" + key1 + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
