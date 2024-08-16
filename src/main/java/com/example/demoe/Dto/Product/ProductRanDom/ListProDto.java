package com.example.demoe.Dto.Product.ProductRanDom;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ListProDto {
    private List<ProDto1> proDtos;
    private String message;
    private Integer totalPage;

    public ListProDto(List<ProDto1> proDtos,Integer totalPage, String message) {
        this.proDtos = proDtos;
        this.message = message;
        this.totalPage=totalPage;
    }
    public ListProDto(String message){
        this.message = message;
    }
}
