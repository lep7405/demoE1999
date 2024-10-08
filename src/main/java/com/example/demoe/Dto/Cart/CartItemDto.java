package com.example.demoe.Dto.Cart;

import com.example.demoe.Dto.Discount.DiscountDtoCartRedis;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CartItemDto {

    private long id;
    private long productId;
    private String productName;
    private int quantity;
    private long provarId;
    private BigDecimal price;
    private DiscountDtoCartRedis discount;
    private String image;
    private List<Vars> varList;
    private Integer max1Buy;
}
