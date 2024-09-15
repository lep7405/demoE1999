package com.example.demoe.Controller.ProductController.UpdateProductRequest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Getter
@Setter
public class ExtraUpdate {
    private Long id;
    private String price;
    private String stock;
    private MultipartFile file;
    private String image;
    private List<ExtraValueUpdate> extraValue;

    @Override
    public String toString() {
        return "ExtraProductVariant{" +
                "price='" + price + '\'' +
                ", stock='" + stock + '\'' +
                ", extraValue=" + extraValue +
                '}';
    }
}
