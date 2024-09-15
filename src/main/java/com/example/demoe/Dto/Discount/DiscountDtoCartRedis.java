package com.example.demoe.Dto.Discount;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class DiscountDtoCartRedis {

    @Builder.Default
    private LocalDateTime startDate = null;
    @Builder.Default
    private LocalDateTime endDate = null;
    @Builder.Default
    private BigDecimal discountValue = BigDecimal.ZERO;
}
