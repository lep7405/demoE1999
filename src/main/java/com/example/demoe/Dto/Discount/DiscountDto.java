package com.example.demoe.Dto.Discount;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class DiscountDto {
    private Integer id;
    private String Type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer level;
    private Integer discountValue;
    private Boolean isActive;
}
