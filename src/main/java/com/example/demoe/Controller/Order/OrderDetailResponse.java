package com.example.demoe.Controller.Order;

import com.example.demoe.Dto.OrderPaid.OrderPaidItemDto;
import com.example.demoe.Entity.Shipping.Shipping;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderDetailResponse {
    private List<OrderPaidItemDto> orderPaidItemDtos ;
    private Shipping shipping;

}
