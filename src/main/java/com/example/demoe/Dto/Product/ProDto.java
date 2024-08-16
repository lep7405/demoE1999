package com.example.demoe.Dto.Product;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProDto {
    private String message;
    private Short totalPage;
    private List<ProductDetailDto> productDetailDtos;

    public ProDto(String message,Short totalPage,List<ProductDetailDto> productDetailDtos) {
        this.message = message;
        this.totalPage=totalPage;
        this.productDetailDtos = productDetailDtos;
    }
    public ProDto(String message) {
        this.message = message;
    }
    public ProDto(){}
}
