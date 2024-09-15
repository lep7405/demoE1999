package com.example.demoe.Dto.Product;

import com.example.demoe.Dto.CommentDto;
import com.example.demoe.Entity.product.Category;
import com.example.demoe.Entity.product.Discount;
import com.example.demoe.Entity.product.ProVar;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDto {
    private Long id;
    private String productName;
    private String description;
    private String brand;
    private Boolean active;
    private String image;
    private BigDecimal price;
    private List<Discount> discountList;
    private Integer totalStock;
    private Short totalVariant;

    private List<CommentDto> comments;
    private Integer max1Buy;
    private List<Category> categories;
    private List<ProVar> proVarList;
    private List<String> images;
}
