package com.bankmanagement.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import com.bankmanagement.dao.AccountDAO;
import com.bankmanagement.dao.TransactionDAO;
import com.bankmanagement.exception.AccountNotFoundException;
import com.bankmanagement.exception.InsufficientBalanceException;
import com.bankmanagement.exception.InvalidAmountException;
import com.bankmanagement.model.Account;
import com.bankmanagement.model.BankTransaction;
import com.bankmanagement.util.DBConnection;

public class TransactionService implements TransactionServiceInterface {
    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final Random random = new Random();

    public TransactionService() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
    }

    public TransactionService(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public void deposit(String accountNumber, double amount, String description) throws Exception {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero");
        }

        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account number " + accountNumber + " not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalStateException("Account is not active");
        }

        double newBalance = account.getBalance() + amount;
        
        // Save balance and transaction record
        boolean success = accountDAO.updateBalance(account.getAccountId(), newBalance);
        if (!success) {
            throw new Exception("Failed to update account balance");
        }

        BankTransaction tx = new BankTransaction();
        tx.setAccountId(account.getAccountId());
        tx.setTransactionType("DEPOSIT");
        tx.setAmount(amount);
        tx.setDescription(description != null && !description.trim().isEmpty() ? description : "Cash Deposit");
        tx.setReferenceNumber(generateReferenceNumber());
        tx.setTransactionDate(LocalDateTime.now());
        tx.setStatus("SUCCESS");

        transactionDAO.save(tx);
    }

    @Override
    public void withdraw(String accountNumber, double amount, String description) throws Exception {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero");
        }

        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account number " + accountNumber + " not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new IllegalStateException("Account is not active");
        }

        // Polymorphic rule validation check (Savings vs Current limit check)
        if (!account.canWithdraw(amount)) {
            throw new InsufficientBalanceException("Insufficient balance to perform withdrawal");
        }

        double newBalance = account.getBalance() - amount;
        boolean success = accountDAO.updateBalance(account.getAccountId(), newBalance);
        if (!success) {
            throw new Exception("Failed to update account balance");
        }

        BankTransaction tx = new BankTransaction();
        tx.setAccountId(account.getAccountId());
        tx.setTransactionType("WITHDRAW");
        tx.setAmount(amount);
        tx.setDescription(description != null && !description.trim().isEmpty() ? description : "Cash Withdrawal");
        tx.setReferenceNumber(generateReferenceNumber());
        tx.setTransactionDate(LocalDateTime.now());
        tx.setStatus("SUCCESS");

        transactionDAO.save(tx);
    }

    @Override
    public void transfer(String senderAccountNumber, String receiverAccountNumber, double amount, String description) throws Exception {
        if (amount <= 0) {
            throw new InvalidAmountException("Transfer amount must be greater than zero");
        }

        if (senderAccountNumber == null || receiverAccountNumber == null || senderAccountNumber.equalsIgnoreCase(receiverAccountNumber)) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }

        Account sender = accountDAO.findByAccountNumber(senderAccountNumber);
        if (sender == null) {
            throw new AccountNotFoundException("Sender account " + senderAccountNumber + " not found");
        }

        Account receiver = accountDAO.findByAccountNumber(receiverAccountNumber);
        if (receiver == null) {
            throw new AccountNotFoundException("Receiver account " + receiverAccountNumber + " not found");
        }

        if (!"ACTIVE".equalsIgnoreCase(sender.getStatus())) {
            throw new IllegalStateException("Sender account is not active");
        }

        if (!"ACTIVE".equalsIgnoreCase(receiver.getStatus())) {
            throw new IllegalStateException("Receiver account is not active");
        }

        // Validate funds polymorphically
        if (!sender.canWithdraw(amount)) {
            throw new InsufficientBalanceException("Insufficient balance for fund transfer");
        }

        // Begin transaction management using raw JDBC
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Debit sender
                double newSenderBalance = sender.getBalance() - amount;
                boolean debitSuccess = accountDAO.updateBalance(conn, sender.getAccountId(), newSenderBalance);
                if (!debitSuccess) {
                    throw new SQLException("Failed to debit sender account");
                }

                // 2. Credit receiver
                double newReceiverBalance = receiver.getBalance() + amount;
                boolean creditSuccess = accountDAO.updateBalance(conn, receiver.getAccountId(), newReceiverBalance);
                if (!creditSuccess) {
                    throw new SQLException("Failed to credit receiver account");
                }

                // Create shared reference number
                String referenceNumber = generateReferenceNumber();
                String descStr = description != null && !description.trim().isEmpty() ? description : "Fund Transfer";

                // 3. Save sender transaction record
                BankTransaction senderTx = new BankTransaction();
                senderTx.setAccountId(sender.getAccountId());
                senderTx.setTransactionType("TRANSFER");
                senderTx.setAmount(amount);
                senderTx.setDescription(descStr + " to " + receiverAccountNumber);
                senderTx.setReferenceNumber(referenceNumber);
                senderTx.setTransactionDate(LocalDateTime.now());
                senderTx.setStatus("SUCCESS");
                transactionDAO.save(conn, senderTx);

                // 4. Save receiver transaction record
                BankTransaction receiverTx = new BankTransaction();
                receiverTx.setAccountId(receiver.getAccountId());
                receiverTx.setTransactionType("TRANSFER");
                receiverTx.setAmount(amount);
                receiverTx.setDescription(descStr + " from " + senderAccountNumber);
                receiverTx.setReferenceNumber(referenceNumber + "-R");
                receiverTx.setTransactionDate(LocalDateTime.now());
                receiverTx.setStatus("SUCCESS");
                transactionDAO.save(conn, receiverTx);

                // 5. Commit transaction
                conn.commit();
            } catch (Exception e) {
                // Rollback if any step fails
                conn.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<BankTransaction> getTransactionHistory(String accountNumber) throws Exception {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account number " + accountNumber + " not found");
        }
        return transactionDAO.findByAccountId(account.getAccountId());
    }

    private String generateReferenceNumber() {
        return "TXN" + System.currentTimeMillis() + (1000 + random.nextInt(9000));
    }
}
