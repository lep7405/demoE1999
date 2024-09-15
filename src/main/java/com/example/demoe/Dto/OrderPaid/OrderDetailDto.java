package com.example.demoe.Dto.OrderPaid;

import com.example.demoe.Entity.Shipping.Shipping;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderDetailDto {
    private Shipping shipping;
    private OrderPaidItemDto orderPaidItemDto;
}
