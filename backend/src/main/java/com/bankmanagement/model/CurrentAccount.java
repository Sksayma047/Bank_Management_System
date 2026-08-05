package com.bankmanagement.model;

import java.time.LocalDateTime;

public class CurrentAccount extends Account {
    private static final double INTEREST_RATE = 0.0; // No interest for current accounts
    private static final double OVERDRAFT_LIMIT = 1000.0; // Allowed overdraft up to $1000

    public CurrentAccount() {
        super();
        setAccountType("CURRENT");
    }

    public CurrentAccount(int accountId, String accountNumber, int customerId, double balance, String status, LocalDateTime createdAt) {
        super(accountId, accountNumber, customerId, "CURRENT", balance, status, createdAt);
    }

    @Override
    public double getInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public boolean canWithdraw(double amount) {
        // Can withdraw up to the overdraft limit
        return (getBalance() - amount) >= -OVERDRAFT_LIMIT;
    }
}
