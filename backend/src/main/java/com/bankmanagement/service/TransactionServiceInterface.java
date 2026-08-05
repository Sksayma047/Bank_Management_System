package com.bankmanagement.service;

import java.util.List;
import com.bankmanagement.model.BankTransaction;

public interface TransactionServiceInterface {
    
    /**
     * Deposits a given amount into an active account.
     */
    void deposit(String accountNumber, double amount, String description) throws Exception;

    /**
     * Withdraws a given amount from an active account.
     */
    void withdraw(String accountNumber, double amount, String description) throws Exception;

    /**
     * Transfers a given amount between two active accounts.
     */
    void transfer(String senderAccountNumber, String receiverAccountNumber, double amount, String description) throws Exception;

    /**
     * Retrieves the transaction history list for a given account number.
     */
    List<BankTransaction> getTransactionHistory(String accountNumber) throws Exception;
}
