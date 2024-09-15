package com.example.demoe.Dto.OrderPaid;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderPaidItemDto {
    private Long id;
    private Short quantity;
    private BigDecimal shippingFee;
    private Long productId;
    private String productName;
    private String image;
    private Long productVarId;
    List<ProVarDto> proVarDtos;
    private BigDecimal provarPrice;
    @Override
    public String toString() {
        return "OrderPaidItemDto{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", shippingFee=" + shippingFee +
                ", productId=" + productId +
                '}';
    }
}
