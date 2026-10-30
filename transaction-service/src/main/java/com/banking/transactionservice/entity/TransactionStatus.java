package com.banking.transactionservice.entity;

/**
 * Transaction Lifecyle flow:
 * PENDING -> PROCESSING ->COMPLETED (Clean transaction)
 *                       ->PENDING-VERIFICATION(Suspicious detected)
 *                                -> COMPLETED (Verified)
 *                                -> FLAGGED (SAGA REFUND)
 *                       ->FAILED
 *                       ->FLAGGED
 * @author Praveen Chauhan
 */

public enum TransactionStatus {

	PENDING,
	PROCESSING,
	PENDING_VERIFICATION,
	COMPLETED,
	FAILED,
	FLAGGED
}
