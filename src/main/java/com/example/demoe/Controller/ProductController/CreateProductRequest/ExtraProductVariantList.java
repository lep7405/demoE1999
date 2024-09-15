package com.example.demoe.Controller.ProductController.CreateProductRequest;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demoe.Controller.ProductController.CreateProductRequest.ExtraProductVariant;
import com.example.demoe.Controller.ProductController.CreateProductRequest.ExtraValue;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtraProductVariantList {
    private String productName;
    private String description;
    private Integer max1Buy;
    private Integer categoryId;
    private Boolean active;
    private MultipartFile[] files;
    private List<ExtraProductVariant> extraProductVariant;
    public void setProductVariants(List<ExtraProductVariant> extraProductVariant) {
        this.extraProductVariant = extraProductVariant;
    }

    // Getter method for productVariants
    public List<ExtraProductVariant> getProductVariants() {
        return extraProductVariant;
    }
    // Getter and Setter

}
