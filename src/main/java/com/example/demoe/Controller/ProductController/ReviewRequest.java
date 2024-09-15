package com.example.demoe.Controller.ProductController;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
@Getter
@Setter
public class ReviewRequest {
//    @NotNull(message = "id must be provided")
    private Long productId;
//    @NotNull(message = "Content is required")
    private String content;
    private Integer rateNumber;
//    @NotNull(message = "Files must be provided")
    private MultipartFile[] files;
}
