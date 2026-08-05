package com.bankmanagement.model;

import java.time.LocalDateTime;

public class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.025; // 2.5% interest rate
    private static final double MINIMUM_BALANCE = 100.0; // Minimum balance limit

    public SavingsAccount() {
        super();
        setAccountType("SAVINGS");
    }

    public SavingsAccount(int accountId, String accountNumber, int customerId, double balance, String status, LocalDateTime createdAt) {
        super(accountId, accountNumber, customerId, "SAVINGS", balance, status, createdAt);
    }

    @Override
    public double getInterestRate() {
        return INTEREST_RATE;
    }

    @Override
    public boolean canWithdraw(double amount) {
        // Balance cannot drop below the minimum balance for Savings
        return (getBalance() - amount) >= MINIMUM_BALANCE;
    }
}
