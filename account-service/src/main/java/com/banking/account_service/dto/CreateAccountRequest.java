package com.banking.account_service.dto;

import java.math.BigDecimal;

import com.banking.account_service.entity.AccountType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

	@NotBlank(message = "Account holder name is required")
	private String accountHolderName;
	
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;
	
	@NotBlank(message = "Phone number is required")
	private String phone;
	
	@NotNull (message = "AccountType is required")
	private AccountType accountType;
	
	@NotNull (message = "Initial Deposit is required")
	@Positive(message = "Initial Deposit must be positive")
	private BigDecimal initialDeposit;
}
