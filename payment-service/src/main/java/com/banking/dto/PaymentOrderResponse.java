package com.banking.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private String paymentId;

    private String razorpayOrderId;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String razorpayKeyId;
}
