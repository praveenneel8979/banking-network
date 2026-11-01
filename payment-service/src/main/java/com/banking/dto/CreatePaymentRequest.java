package com.banking.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotBlank(message = "Account Number is required")
    private String accountNumber;

    @NotBlank(message = "Account is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String description;

}
