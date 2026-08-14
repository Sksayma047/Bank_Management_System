package com.bankmanagement.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.bankmanagement.model.BankTransaction;
import com.bankmanagement.util.DBConnection;

public class TransactionDAO {

    public BankTransaction findById(int id) {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error looking up transaction by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<BankTransaction> findByAccountId(int accountId) {
        List<BankTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error looking up transactions by account ID: " + e.getMessage(), e);
        }
        return list;
    }

    public BankTransaction save(BankTransaction t) {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, reference_number, transaction_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            setStatementParameters(ps, t);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        t.setTransactionId(generatedKeys.getInt(1));
                        return t;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error saving transaction: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Saves a transaction using an existing active database connection (for JDBC transaction support).
     */
    public BankTransaction save(Connection conn, BankTransaction t) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, reference_number, transaction_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(ps, t);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        t.setTransactionId(generatedKeys.getInt(1));
                        return t;
                    }
                }
            }
        }
        return null;
    }

    private void setStatementParameters(PreparedStatement ps, BankTransaction t) throws SQLException {
        ps.setInt(1, t.getAccountId());
        ps.setString(2, t.getTransactionType());
        ps.setDouble(3, t.getAmount());
        ps.setString(4, t.getDescription());
        ps.setString(5, t.getReferenceNumber());
        ps.setTimestamp(6, Timestamp.valueOf(t.getTransactionDate() != null ? t.getTransactionDate() : LocalDateTime.now()));
        ps.setString(7, t.getStatus());
    }

    private BankTransaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        BankTransaction t = new BankTransaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setAccountId(rs.getInt("account_id"));
        t.setTransactionType(rs.getString("transaction_type"));
        t.setAmount(rs.getDouble("amount"));
        t.setDescription(rs.getString("description"));
        t.setReferenceNumber(rs.getString("reference_number"));
        
        Timestamp ts = rs.getTimestamp("transaction_date");
        if (ts != null) {
            t.setTransactionDate(ts.toLocalDateTime());
        }
        
        t.setStatus(rs.getString("status"));
        return t;
    }
}
