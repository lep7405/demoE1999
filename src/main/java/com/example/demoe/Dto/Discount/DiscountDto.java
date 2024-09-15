package com.example.demoe.Dto.Discount;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DiscountDto {
    private Integer id;
    private String Type;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer level;
    private BigDecimal discountValue;
    private Boolean isActive;
}
