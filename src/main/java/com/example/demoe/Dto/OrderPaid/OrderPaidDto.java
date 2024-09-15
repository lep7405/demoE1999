package com.example.demoe.Dto.OrderPaid;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderPaidDto {
    private Long id;
    private Long txnRep;
    private LocalDate order1Date;
    private String status;

    private BigDecimal price;

    List<OrderPaidItemDto> orderPaidItemDtos;

    @Override
    public String toString() {
        return "OrderPaidDto{" +
                "id=" + id +
                ", txnRep=" + txnRep +
                ", order1Date=" + order1Date +
                ", status='" + status + '\'' +
                ", price=" + price +
                '}';
    }
}
