package com.example.demoe.Dto;

import lombok.Builder;

public abstract class PaymentDto {
    @Builder
    public static class VNPayResponse {
        public String code;
        public String message;
        public String paymentUrl;
        public VNPayResponse(String code, String message, String paymentUrl) {
            this.code = code;
            this.message = message;
            this.paymentUrl = paymentUrl;
        }
    }
}