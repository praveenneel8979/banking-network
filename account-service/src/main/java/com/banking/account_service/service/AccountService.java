package com.banking.account_service.service;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.banking.account_service.dto.AccountResponse;
import com.banking.account_service.dto.CreateAccountRequest;
import com.banking.account_service.entity.Account;
import com.banking.account_service.entity.AccountStatus;
import com.banking.account_service.entity.AccountType;
import com.banking.account_service.repo.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
	
	private final AccountRepository accountRepository;
	private static SecureRandom secureRandom= new SecureRandom();
	
	public AccountResponse createAccount(CreateAccountRequest request) {
		log.info("Create account for: {}", request.getEmail());
		if(accountRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Account already exists for email: "+request.getEmail());
		}
		
		Account account= new Account();
		account.setAccountHolderName(request.getAccountHolderName());
		account.setEmail(request.getEmail());
		account.setPhone(request.getPhone());
		account.setAccountType(request.getAccountType());
		account.setStatus(AccountStatus.ACTIVE);
		account.setBalance(request.getInitialDeposit());
		account.setAccountNumber(generateAccountNumber());
		account.setDailyTransactionLimit(
				request.getAccountType() == AccountType.SAVINGS
				? new BigDecimal("100000")
				: new BigDecimal("500000")
				);
		
		Account saveAccount = accountRepository.save(account);
		log.info("Account created : {}", saveAccount.getAccountNumber());
		return mapToResponse(saveAccount);
	}
	
	/**
	 * Get account by account number
	 * @param accountNumber
	 * @return
	 */

	public AccountResponse getAccount(String accountNumber) {
		Account account= accountRepository.findByAccountNumber(accountNumber).orElseThrow(()-> new RuntimeException("Account not found"));
		return mapToResponse(account);
	}
	
	/**
	 * Get account balance
	 * @param accountNumber
	 * @return
	 */
	public BigDecimal getBalance(String accountNumber) {
		Account account= accountRepository.findByAccountNumber(accountNumber).orElseThrow(()-> new RuntimeException("Account not found"));
		return account.getBalance();
	}
	
	/**
	 * Block account:- called by fraud detection service via kafka 
	 * @param accountNumber
	 */

	public void blockAccount(String accountNumber) {
		log.info("Blocking account:{}", accountNumber);
		Account account= accountRepository.findByAccountNumber(accountNumber).orElseThrow(()-> new RuntimeException("Account not found"));
		account.setStatus(AccountStatus.BLOCKED);
		accountRepository.save(account);
		log.info("Account Blocked: {}", accountNumber);
	}
	
	/**
	 * deduct balance for sender account
	 * called by transaction service
	 * @param accountNumber
	 * @param amount
	 */
	public void deductBalance(String accountNumber, BigDecimal amount) {
		log.info("Deduct balance {} from account : {}", amount, accountNumber);
		
		Account account= accountRepository.findByAccountNumber(accountNumber).orElseThrow(()-> new RuntimeException("Account not found"));
		
		if(account.getStatus() != AccountStatus.ACTIVE) {
			throw new RuntimeException("Account is not active "+accountNumber);
		}
		
		if(account.getBalance().compareTo(amount) < 0) {
			throw new RuntimeException("Insufficient funds for amount "+accountNumber);
		}
		
		account.setBalance(account.getBalance().subtract(amount));
		accountRepository.save(account);
		
	   log.info("Balance updated. new Balance : {}",account.getBalance());
		
	}
	
	/**
	 * Credit balance
	 * called by transaction service via kafka
	 * @param accountNumber
	 * @param amount
	 */
	
	public void creditBalance(String accountNumber, BigDecimal amount) {
		log.info("Crediting {} to account: {}", amount,accountNumber);
		
		Account account= accountRepository.findByAccountNumber(accountNumber).orElseThrow(()-> new RuntimeException("Account not found"));
		
		account.setBalance(account.getBalance().add(amount));
		accountRepository.save(account);
		
		log.info("Balance Credited. New Balance: {}", account.getBalance());
	}
	
	//Generate unique 12 digit account number
	private String generateAccountNumber() {
		String accountNumber;
		
		do {
			
			long number =secureRandom.nextLong(1000000000000L);
			accountNumber = String.format("%012d", number);
			
		}while(accountRepository.existsByAccountNumber(accountNumber));
		
		return accountNumber;
	}
	
	private AccountResponse mapToResponse(Account account) {
		AccountResponse response = new AccountResponse();
		response.setId(account.getId());
		response.setAccountHolderName(account.getAccountHolderName());
		response.setAccountNumber(account.getAccountNumber());
		response.setEmail(account.getEmail());
		response.setPhone(account.getPhone());
		response.setAccountType(account.getAccountType());
		response.setStatus(account.getStatus());
		response.setBalance(account.getBalance());
		response.setDailyTransactionLimit(account.getDailyTransactionLimit());
		response.setCreateAt(account.getCreatedAt());
		
		return response;
	}

}
