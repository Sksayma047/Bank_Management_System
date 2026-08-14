package com.bankmanagement.dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.bankmanagement.model.Customer;
import com.bankmanagement.util.DBConnection;

public class CustomerDAO {

    public Customer findById(int id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error looking up customer by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public Customer findByEmail(String email) {
        String sql = "SELECT * FROM customers WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error looking up customer by email: " + e.getMessage(), e);
        }
        return null;
    }

    public Customer findByPhone(String phone) {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error looking up customer by phone: " + e.getMessage(), e);
        }
        return null;
    }

    public Customer save(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone, address, date_of_birth, password_hash, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setDate(5, Date.valueOf(customer.getDateOfBirth()));
            ps.setString(6, customer.getPasswordHash());
            ps.setTimestamp(7, Timestamp.valueOf(customer.getCreatedAt() != null ? customer.getCreatedAt() : LocalDateTime.now()));
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setCustomerId(generatedKeys.getInt(1));
                        return customer;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error saving customer: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone = ?, address = ?, date_of_birth = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.setDate(5, Date.valueOf(customer.getDateOfBirth()));
            ps.setInt(6, customer.getCustomerId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating customer: " + e.getMessage(), e);
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting customer: " + e.getMessage(), e);
        }
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        
        Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            c.setDateOfBirth(dob.toLocalDate());
        }
        
        c.setPasswordHash(rs.getString("password_hash"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            c.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return c;
    }
}
