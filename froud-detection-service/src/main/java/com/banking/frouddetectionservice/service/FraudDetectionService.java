package com.banking.frouddetectionservice.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.banking.frouddetectionservice.client.AccountServiceClient;
import com.banking.frouddetectionservice.model.FraudCheckResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {
	
	private final AccountServiceClient accountServiceClient;
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	
	private static final String FRAUD_CHECK_CLEAN_TOPIC = "fraud.check.clean";
	private static final String VERIFICATION_REQUIRED_TOPIC ="verification.required";
	
	public void checkTransaction(Map<String, Object> payload) {
		String transactionId =(String) payload.get("transactionId");
		String accountNumber = (String) payload.get("senderAccountNumber");
		BigDecimal amount=new BigDecimal(payload.get("amount").toString());
		
		//Fetch real balance from account service
		BigDecimal senderBalance = accountServiceClient.getBalance();
		
		log.info("Checking transactions: {} account: {} amount: {} balance: {}",
				transactionId,accountNumber,amount,senderBalance);
		
		FraudCheckResult result= performFraudChecks(accountNumber,amount,senderBalance);
		if(result.isFraud()) {
			log.info("Suspicious activity detected - amount: {} " +
		             "reason: {} - requesting OTP verification",accountNumber,result.getReason());
			Map<String, Object> verificationEvent=new HashMap<>();
			verificationEvent.put("transactionId", transactionId);
			verificationEvent.put("accountNumer", accountNumber);
			verificationEvent.put("amount",amount);
			verificationEvent.put("reason", result.getReason());
			
		 kafkaTemplate.send(VERIFICATION_REQUIRED_TOPIC, transactionId,verificationEvent);
			
		}else {
			//transaction is clean
			log.info("Transaction clean);"
			Map<String, Object> transactionCleanEvent = new HashMap<>();
			transactionCleanEvent.put("transactionId", transactionId);
			transactionCleanEvent.put("isFraud",false);
			transactionCleanEvent.put("reason", null);
			
			kafkaTemplate.send(FRAUD_CHECK_CLEAN_TOPIC,transactionId,transactionCleanEvent);
		}
		
	}
	
	private FraudCheckResult performFraudChecks(String accountNumber,
			                                    BigDecimal amount,
			                                    BigDecimal senderBalance) {
		//Pattern 1: Velocity check
		if(isVelocityExceeded(accountNumber)) {
			return new FraudCheckResult(true, "Too many transactions in 60 seconds" + "- Velocity limit exceeded");
		}
		
		//Pattern 2: Amount check
		
		if(isAmountSuspicious(accountNumber,amount)) {
			return new FraudCheckResult(true, "Unsual transaction amount" + " - exceeds 3x your average");
		}
		
		//Patten 3: Balance Check
		if(senderBalance.compareTo(BigDecimal.ZERO) > 0 && isBalanceCheckFailed(senderBalance, amount)) {
			return new FraudCheckResult(true, "Transaction exceed 90% of account balance");
		}
		
		return new FraudCheckResult(false,null);
	}

}
