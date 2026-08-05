package com.bankmanagement.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import com.bankmanagement.exception.AccountNotFoundException;
import com.bankmanagement.model.Account;
import com.bankmanagement.service.AccountService;

public class AccountController extends BaseController {
    private final AccountService accountService;

    public AccountController() {
        this.accountService = new AccountService();
    }

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int accountId = getIdFromPath(req);
        Integer tokenCustomerId = (Integer) req.getAttribute("customerId");

        if (accountId == -1) {
            // Path is empty: retrieve all accounts belonging to the logged-in customer
            List<Account> accounts = accountService.getAccountsByCustomerId(tokenCustomerId);
            writeJson(resp, HttpServletResponse.SC_OK, accounts);
        } else {
            // Retrieve a single account
            try {
                Account account = accountService.getAccountById(accountId);
                // Security Check: Customer can only view their own accounts
                if (account.getCustomerId() != tokenCustomerId) {
                    writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not own this account");
                    return;
                }
                writeJson(resp, HttpServletResponse.SC_OK, account);
            } catch (AccountNotFoundException e) {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            } catch (Exception e) {
                writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred retrieving account");
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer tokenCustomerId = (Integer) req.getAttribute("customerId");
        
        try {
            AccountOpenRequest openReq = readJson(req, AccountOpenRequest.class);
            if (openReq == null || openReq.getAccountType() == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Account type and initial deposit are required");
                return;
            }

            Account account = accountService.openAccount(tokenCustomerId, openReq.getAccountType(), openReq.getInitialDeposit());
            writeJson(resp, HttpServletResponse.SC_CREATED, account);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // Request DTO helper class
    private static class AccountOpenRequest {
        private String accountType;
        private double initialDeposit;

        public String getAccountType() {
            return accountType;
        }

        public double getInitialDeposit() {
            return initialDeposit;
        }
    }
}
