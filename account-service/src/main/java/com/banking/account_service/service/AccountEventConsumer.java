package com.banking.account_service.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountEventConsumer {
	
	private final AccountService accountService;
	
	/**
	 * Consume transaction completed event from kafka
	 * Credits receiver account
	 * @param payload
	 */
	
	@KafkaListener(topics = "transaction.completed")
	public void consumeTransactionComplete(@Payload Map<String, Object> payload) {
		try {
			
			String receiverAccount = (String)payload.get("receiverAccountNumber");
			BigDecimal amount=  new BigDecimal(payload.get("amount").toString());
			
			log.info("Crediting account: {} amount: {}", receiverAccount, amount);
			accountService.creditBalance(receiverAccount, amount);
			
		}catch (Exception e) {
			log.info("Error crediting account: {}", e.getMessage());
		}
	}

}
