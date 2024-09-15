package com.example.demoe.Controller.ProductController.CreateProductRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtraValue {
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
