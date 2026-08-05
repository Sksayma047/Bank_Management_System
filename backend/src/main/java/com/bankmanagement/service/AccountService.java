package com.bankmanagement.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import com.bankmanagement.dao.AccountDAO;
import com.bankmanagement.exception.AccountNotFoundException;
import com.bankmanagement.model.Account;
import com.bankmanagement.model.CurrentAccount;
import com.bankmanagement.model.SavingsAccount;

public class AccountService {
    private final AccountDAO accountDAO;
    private final Random random = new Random();

    public AccountService() {
        this.accountDAO = new AccountDAO();
    }

    public AccountService(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    /**
     * Opens a new account for a customer.
     */
    public Account openAccount(int customerId, String accountType, double initialDeposit) throws Exception {
        if (accountType == null || (!accountType.equalsIgnoreCase("SAVINGS") && !accountType.equalsIgnoreCase("CURRENT"))) {
            throw new IllegalArgumentException("Invalid account type. Must be SAVINGS or CURRENT");
        }

        if (initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }

        Account account;
        if (accountType.equalsIgnoreCase("SAVINGS")) {
            if (initialDeposit < 100.0) {
                throw new IllegalArgumentException("Minimum opening balance for a Savings Account is $100.00");
            }
            account = new SavingsAccount();
        } else {
            account = new CurrentAccount();
        }

        account.setCustomerId(customerId);
        account.setBalance(initialDeposit);
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        
        // Generate a unique account number and verify it doesn't exist
        String accountNumber = generateUniqueAccountNumber(accountType);
        account.setAccountNumber(accountNumber);

        Account saved = accountDAO.save(account);
        if (saved == null) {
            throw new Exception("Failed to save new bank account");
        }
        return saved;
    }

    /**
     * Retrieves an account by its ID.
     */
    public Account getAccountById(int accountId) throws AccountNotFoundException {
        Account acc = accountDAO.findById(accountId);
        if (acc == null) {
            throw new AccountNotFoundException("Account with ID " + accountId + " not found");
        }
        return acc;
    }

    /**
     * Retrieves an account by its account number.
     */
    public Account getAccountByNumber(String accountNumber) throws AccountNotFoundException {
        Account acc = accountDAO.findByAccountNumber(accountNumber);
        if (acc == null) {
            throw new AccountNotFoundException("Account number " + accountNumber + " not found");
        }
        return acc;
    }

    /**
     * Retrieves all accounts owned by a specific customer.
     */
    public List<Account> getAccountsByCustomerId(int customerId) {
        return accountDAO.findByCustomerId(customerId);
    }

    /**
     * Helper method to generate unique 10-digit account numbers prefixed by account type.
     */
    private String generateUniqueAccountNumber(String accountType) {
        String prefix = accountType.equalsIgnoreCase("SAVINGS") ? "SAV-" : "CUR-";
        String accountNumber;
        do {
            long suffix = 1000000000L + (long)(random.nextDouble() * 900000000L);
            accountNumber = prefix + suffix;
        } while (accountDAO.findByAccountNumber(accountNumber) != null);
        
        return accountNumber;
    }
}
