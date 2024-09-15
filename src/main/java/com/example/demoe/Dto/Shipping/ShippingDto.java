package com.example.demoe.Dto.Shipping;

import com.example.demoe.Dto.OrderPaid.OrderPaidItemDto;
import com.example.demoe.Dto.OrderPaid.ProVarDto;
import com.example.demoe.Entity.Address.Address;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ShippingDto {
    private Long id;
    private LocalDateTime deliveryTime;
    private LocalDateTime shippingTime;
    private LocalDateTime receivedTime;
    private BigDecimal totalProductPrice;

    private Address address;
//    private Long orderItemId;
//    private Short quantity;
//    private String image;
    private String status;
//    private Long productId;
//    private String productName;
    private String orderStatus;
   List<OrderPaidItemDto> orderPaidItemDtos;

}
