package com.example.demoe.Dto;

import com.example.demoe.Dto.Product.ProductDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class ReviewDto {
    private Long id;
    private String content;
    private LocalDate commentTime;
    private Integer rateNumber;
    private List<String> contextImage;
    private String productName;
    private Long productId;
    private String productImage;

}
