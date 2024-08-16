package com.example.demoe.Controller.ProductController.CreateProductRequest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter

public class ExtraProductVariant {
    private String price;
    private String stock;
    private MultipartFile file;
    private List<ExtraValue> extraValue;

    @Override
    public String toString() {
        return "ExtraProductVariant{" +
                "price='" + price + '\'' +
                ", stock='" + stock + '\'' +
                ", extraValue=" + extraValue +
                '}';
    }
}
