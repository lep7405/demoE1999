package com.example.demoe.Controller.ProductController.UpdateProductRequest;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtraUpdateList {
    private String productName;
    private String description;
    private Integer max1Buy;
    private Integer categoryId;
    private Boolean active;
    private MultipartFile[] files;
    private List<String> images;
    private List<ExtraUpdate> extraUpdates;
    private Long id;
    public void setProductVariants(List<ExtraUpdate> extraUpdates) {
        this.extraUpdates = extraUpdates;
    }

    // Getter method for productVariants
    public List<ExtraUpdate> getProductVariants() {
        return extraUpdates;
    }
    // Getter and Setter

}
