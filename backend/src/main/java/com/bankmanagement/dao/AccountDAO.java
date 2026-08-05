package com.bankmanagement.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.bankmanagement.model.Account;
import com.bankmanagement.model.CurrentAccount;
import com.bankmanagement.model.SavingsAccount;
import com.bankmanagement.util.DBConnection;

public class AccountDAO {

    public Account findById(int id) {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Account findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Account> findByCustomerId(int customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapResultSetToAccount(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public Account save(Account account) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance, status, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, account.getAccountNumber());
            ps.setInt(2, account.getCustomerId());
            ps.setString(3, account.getAccountType());
            ps.setDouble(4, account.getBalance());
            ps.setString(5, account.getStatus());
            ps.setTimestamp(6, Timestamp.valueOf(account.getCreatedAt() != null ? account.getCreatedAt() : LocalDateTime.now()));
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        account.setAccountId(generatedKeys.getInt(1));
                        return account;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Specialized updateBalance method that can accept an active connection for transaction operations.
     */
    public boolean updateBalance(Connection conn, int accountId, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int accountId, String status) {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, status);
            ps.setInt(2, accountId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String accountType = rs.getString("account_type");
        Account acc;
        if ("SAVINGS".equalsIgnoreCase(accountType)) {
            acc = new SavingsAccount();
        } else {
            acc = new CurrentAccount();
        }
        
        acc.setAccountId(rs.getInt("account_id"));
        acc.setAccountNumber(rs.getString("account_number"));
        acc.setCustomerId(rs.getInt("customer_id"));
        acc.setBalance(rs.getDouble("balance"));
        acc.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            acc.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return acc;
    }
}
