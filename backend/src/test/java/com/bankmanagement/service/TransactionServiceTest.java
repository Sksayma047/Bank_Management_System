package com.bankmanagement.service;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.bankmanagement.dao.AccountDAO;
import com.bankmanagement.dao.TransactionDAO;
import com.bankmanagement.exception.AccountNotFoundException;
import com.bankmanagement.exception.InsufficientBalanceException;
import com.bankmanagement.exception.InvalidAmountException;
import com.bankmanagement.model.Account;
import com.bankmanagement.model.CurrentAccount;
import com.bankmanagement.model.SavingsAccount;
import com.bankmanagement.model.BankTransaction;
import com.bankmanagement.util.DBConnection;

public class TransactionServiceTest {

    private StubAccountDAO stubAccountDAO;
    private StubTransactionDAO stubTransactionDAO;
    private TransactionService transactionService;
    private Connection mockConnection;
    
    // Trackers for connection calls
    private boolean setAutoCommitCalled;
    private boolean setAutoCommitValue;
    private boolean commitCalled;
    private boolean rollbackCalled;

    // Custom Stub for AccountDAO to bypass Mockito concrete class mocking
    private static class StubAccountDAO extends AccountDAO {
        Account mockAccount1;
        Account mockAccount2;
        boolean updateBalanceSuccess = true;
        
        int updateBalanceCalledCount = 0;
        int updateBalanceWithConnCalledCount = 0;

        @Override
        public Account findByAccountNumber(String accountNumber) {
            if (mockAccount1 != null && accountNumber.equals(mockAccount1.getAccountNumber())) {
                return mockAccount1;
            }
            if (mockAccount2 != null && accountNumber.equals(mockAccount2.getAccountNumber())) {
                return mockAccount2;
            }
            return null;
        }

        @Override
        public boolean updateBalance(int accountId, double newBalance) {
            updateBalanceCalledCount++;
            if (mockAccount1 != null && mockAccount1.getAccountId() == accountId) {
                mockAccount1.setBalance(newBalance);
            }
            return updateBalanceSuccess;
        }

        @Override
        public boolean updateBalance(Connection conn, int accountId, double newBalance) throws SQLException {
            updateBalanceWithConnCalledCount++;
            if (mockAccount1 != null && mockAccount1.getAccountId() == accountId) {
                mockAccount1.setBalance(newBalance);
            }
            if (mockAccount2 != null && mockAccount2.getAccountId() == accountId) {
                mockAccount2.setBalance(newBalance);
            }
            return updateBalanceSuccess;
        }
    }

    // Custom Stub for TransactionDAO to bypass Mockito concrete class mocking
    private static class StubTransactionDAO extends TransactionDAO {
        List<BankTransaction> savedTransactions = new ArrayList<>();

        @Override
        public BankTransaction save(BankTransaction t) {
            savedTransactions.add(t);
            return t;
        }

        @Override
        public BankTransaction save(Connection conn, BankTransaction t) throws SQLException {
            savedTransactions.add(t);
            return t;
        }
    }

    @BeforeEach
    public void setUp() {
        stubAccountDAO = new StubAccountDAO();
        stubTransactionDAO = new StubTransactionDAO();
        transactionService = new TransactionService(stubAccountDAO, stubTransactionDAO);
        
        // Reset connection trackers
        setAutoCommitCalled = false;
        setAutoCommitValue = true;
        commitCalled = false;
        rollbackCalled = false;

        // Use Core Java Dynamic Proxy to mock java.sql.Connection
        mockConnection = (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[] { Connection.class },
            (proxy, method, args) -> {
                String methodName = method.getName();
                if ("setAutoCommit".equals(methodName)) {
                    setAutoCommitCalled = true;
                    setAutoCommitValue = (Boolean) args[0];
                } else if ("commit".equals(methodName)) {
                    commitCalled = true;
                } else if ("rollback".equals(methodName)) {
                    rollbackCalled = true;
                }
                return null;
            }
        );
        
        DBConnection.setTestConnection(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        DBConnection.setTestConnection(null);
    }

    @Test
    public void testDeposit_Success() throws Exception {
        SavingsAccount account = new SavingsAccount(1, "SAV-12345", 100, 500.00, "ACTIVE", LocalDateTime.now());
        stubAccountDAO.mockAccount1 = account;

        transactionService.deposit("SAV-12345", 200.00, "Salary");

        assertEquals(700.00, account.getBalance());
        assertEquals(1, stubAccountDAO.updateBalanceCalledCount);
        assertEquals(1, stubTransactionDAO.savedTransactions.size());
        assertEquals("DEPOSIT", stubTransactionDAO.savedTransactions.get(0).getTransactionType());
    }

    @Test
    public void testDeposit_InvalidAmount() {
        assertThrows(InvalidAmountException.class, () -> {
            transactionService.deposit("SAV-12345", -50.00, "Invalid");
        });
    }

    @Test
    public void testWithdraw_Success() throws Exception {
        SavingsAccount account = new SavingsAccount(1, "SAV-12345", 100, 500.00, "ACTIVE", LocalDateTime.now());
        stubAccountDAO.mockAccount1 = account;

        transactionService.withdraw("SAV-12345", 100.00, "ATM Withdrawal");

        assertEquals(400.00, account.getBalance());
        assertEquals(1, stubAccountDAO.updateBalanceCalledCount);
        assertEquals(1, stubTransactionDAO.savedTransactions.size());
        assertEquals("WITHDRAW", stubTransactionDAO.savedTransactions.get(0).getTransactionType());
    }

    @Test
    public void testWithdraw_InsufficientBalance_Savings() {
        // Savings Account has minimum balance of $100.00. 
        // Initial balance $150.00, withdrawing $100.00 leaves $50.00, which is below min balance!
        SavingsAccount account = new SavingsAccount(1, "SAV-12345", 100, 150.00, "ACTIVE", LocalDateTime.now());
        stubAccountDAO.mockAccount1 = account;

        assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.withdraw("SAV-12345", 100.00, "ATM");
        });
    }

    @Test
    public void testWithdraw_Success_Overdraft_Current() throws Exception {
        // Current Account allows overdraft up to $1000.00
        CurrentAccount account = new CurrentAccount(1, "CUR-12345", 100, 100.00, "ACTIVE", LocalDateTime.now());
        stubAccountDAO.mockAccount1 = account;

        transactionService.withdraw("CUR-12345", 500.00, "Business Expenses");

        assertEquals(-400.00, account.getBalance());
        assertEquals(1, stubAccountDAO.updateBalanceCalledCount);
        assertEquals(1, stubTransactionDAO.savedTransactions.size());
        assertEquals("WITHDRAW", stubTransactionDAO.savedTransactions.get(0).getTransactionType());
    }

    @Test
    public void testTransfer_Success() throws Exception {
        SavingsAccount sender = new SavingsAccount(1, "SAV-111", 100, 1000.00, "ACTIVE", LocalDateTime.now());
        CurrentAccount receiver = new CurrentAccount(2, "CUR-222", 101, 500.00, "ACTIVE", LocalDateTime.now());
        stubAccountDAO.mockAccount1 = sender;
        stubAccountDAO.mockAccount2 = receiver;

        transactionService.transfer("SAV-111", "CUR-222", 200.00, "Dinner split");

        assertEquals(800.00, sender.getBalance());
        assertEquals(700.00, receiver.getBalance());
        assertEquals(2, stubAccountDAO.updateBalanceWithConnCalledCount);
        assertTrue(setAutoCommitCalled);
        assertFalse(setAutoCommitValue);
        assertTrue(commitCalled);
        assertFalse(rollbackCalled);
        assertEquals(2, stubTransactionDAO.savedTransactions.size()); // one sender tx, one receiver tx
    }

    @Test
    public void testTransfer_RollbackOnFailure() throws Exception {
        SavingsAccount sender = new SavingsAccount(1, "SAV-111", 100, 1000.00, "ACTIVE", LocalDateTime.now());
        CurrentAccount receiver = new CurrentAccount(2, "CUR-222", 101, 500.00, "ACTIVE", LocalDateTime.now());
        stubAccountDAO.mockAccount1 = sender;
        stubAccountDAO.mockAccount2 = receiver;

        // Stub updateBalance (with connection) to throw an error on the second call (receiver credit)
        StubAccountDAO errorAccountDAO = new StubAccountDAO() {
            @Override
            public boolean updateBalance(Connection conn, int accountId, double newBalance) throws SQLException {
                if (accountId == 2) {
                    throw new SQLException("DB Connection Lost");
                }
                return super.updateBalance(conn, accountId, newBalance);
            }
        };
        errorAccountDAO.mockAccount1 = sender;
        errorAccountDAO.mockAccount2 = receiver;
        
        TransactionService serviceWithError = new TransactionService(errorAccountDAO, stubTransactionDAO);

        assertThrows(SQLException.class, () -> {
            serviceWithError.transfer("SAV-111", "CUR-222", 200.00, "Refund");
        });

        assertTrue(setAutoCommitCalled);
        assertFalse(setAutoCommitValue);
        assertTrue(rollbackCalled);
        assertFalse(commitCalled);
    }
}
