package com.bankmanagement.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.bankmanagement.exception.AccountNotFoundException;
import com.bankmanagement.exception.InsufficientBalanceException;
import com.bankmanagement.exception.InvalidAmountException;
import com.bankmanagement.model.Account;
import com.bankmanagement.model.BankTransaction;
import com.bankmanagement.service.AccountService;
import com.bankmanagement.service.TransactionService;

public class TransactionController extends BaseController {
    private final TransactionService transactionService;
    private final AccountService accountService;

    public TransactionController() {
        this.transactionService = new TransactionService();
        this.accountService = new AccountService();
    }

    public TransactionController(TransactionService transactionService, AccountService accountService) {
        this.transactionService = transactionService;
        this.accountService = accountService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        Integer tokenCustomerId = (Integer) req.getAttribute("customerId");

        if (pathInfo == null || pathInfo.equals("/")) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Account number is required to fetch history");
            return;
        }

        // The path info contains the account number (e.g., /api/transactions/SAV-1000000001 -> /SAV-1000000001)
        String accountNumber = pathInfo.substring(1);

        try {
            // Owner verification
            Account account = accountService.getAccountByNumber(accountNumber);
            if (account.getCustomerId() != tokenCustomerId) {
                writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not own this account");
                return;
            }

            List<BankTransaction> history = transactionService.getTransactionHistory(accountNumber);
            writeJson(resp, HttpServletResponse.SC_OK, history);
        } catch (AccountNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred retrieving history");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        Integer tokenCustomerId = (Integer) req.getAttribute("customerId");

        if (pathInfo == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            return;
        }

        try {
            if (pathInfo.equals("/deposit")) {
                handleDeposit(req, resp, tokenCustomerId);
            } else if (pathInfo.equals("/withdraw")) {
                handleWithdraw(req, resp, tokenCustomerId);
            } else if (pathInfo.equals("/transfer")) {
                handleTransfer(req, resp, tokenCustomerId);
            } else {
                writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Action not found");
            }
        } catch (InvalidAmountException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (InsufficientBalanceException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (AccountNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void handleDeposit(HttpServletRequest req, HttpServletResponse resp, int tokenCustomerId) throws Exception {
        TransactionRequest transReq = readJson(req, TransactionRequest.class);
        if (transReq == null || transReq.getAccountNumber() == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Account number and amount are required");
            return;
        }

        // Validate ownership for self-service deposits
        Account account = accountService.getAccountByNumber(transReq.getAccountNumber());
        if (account.getCustomerId() != tokenCustomerId) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not own this account");
            return;
        }

        transactionService.deposit(transReq.getAccountNumber(), transReq.getAmount(), transReq.getDescription());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Deposit of $" + transReq.getAmount() + " completed successfully");
        writeJson(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleWithdraw(HttpServletRequest req, HttpServletResponse resp, int tokenCustomerId) throws Exception {
        TransactionRequest transReq = readJson(req, TransactionRequest.class);
        if (transReq == null || transReq.getAccountNumber() == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Account number and amount are required");
            return;
        }

        // Validate ownership
        Account account = accountService.getAccountByNumber(transReq.getAccountNumber());
        if (account.getCustomerId() != tokenCustomerId) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not own this account");
            return;
        }

        transactionService.withdraw(transReq.getAccountNumber(), transReq.getAmount(), transReq.getDescription());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Withdrawal of $" + transReq.getAmount() + " completed successfully");
        writeJson(resp, HttpServletResponse.SC_OK, result);
    }

    private void handleTransfer(HttpServletRequest req, HttpServletResponse resp, int tokenCustomerId) throws Exception {
        TransferRequest transferReq = readJson(req, TransferRequest.class);
        if (transferReq == null || transferReq.getSenderAccountNumber() == null || transferReq.getReceiverAccountNumber() == null) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Sender account, receiver account, and amount are required");
            return;
        }

        // Validate sender ownership
        Account sender = accountService.getAccountByNumber(transferReq.getSenderAccountNumber());
        if (sender.getCustomerId() != tokenCustomerId) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You do not own the source account");
            return;
        }

        transactionService.transfer(
            transferReq.getSenderAccountNumber(), 
            transferReq.getReceiverAccountNumber(), 
            transferReq.getAmount(), 
            transferReq.getDescription()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Transfer of $" + transferReq.getAmount() + " to account " + transferReq.getReceiverAccountNumber() + " completed successfully");
        writeJson(resp, HttpServletResponse.SC_OK, result);
    }

    // Helper request body mappings
    private static class TransactionRequest {
        private String accountNumber;
        private double amount;
        private String description;

        public String getAccountNumber() { return accountNumber; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
    }

    private static class TransferRequest {
        private String senderAccountNumber;
        private String receiverAccountNumber;
        private double amount;
        private String description;

        public String getSenderAccountNumber() { return senderAccountNumber; }
        public String getReceiverAccountNumber() { return receiverAccountNumber; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
    }
}
