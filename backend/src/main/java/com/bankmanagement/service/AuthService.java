package com.bankmanagement.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.bankmanagement.dao.CustomerDAO;
import com.bankmanagement.exception.AuthenticationException;
import com.bankmanagement.model.Customer;
import com.bankmanagement.util.PasswordUtil;

public class AuthService {
    private final CustomerDAO customerDAO;
    
    // Thread-safe map to store token -> Customer ID session mappings
    private static final Map<String, Integer> activeSessions = new ConcurrentHashMap<>();

    public AuthService() {
        this.customerDAO = new CustomerDAO();
    }

    public AuthService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    /**
     * Authenticates a customer by email and password.
     * Returns a session token if successful, throws AuthenticationException otherwise.
     */
    public String login(String email, String password) throws AuthenticationException {
        if (email == null || password == null) {
            throw new AuthenticationException("Email and password cannot be null");
        }

        Customer customer = customerDAO.findByEmail(email);
        if (customer == null) {
            throw new AuthenticationException("Invalid email or password");
        }

        boolean match = PasswordUtil.checkPassword(password, customer.getPasswordHash());
        if (!match) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate a new secure session token
        String token = UUID.randomUUID().toString();
        activeSessions.put(token, customer.getCustomerId());
        return token;
    }

    /**
     * Terminate the session by invalidating the token.
     */
    public void logout(String token) {
        if (token != null) {
            activeSessions.remove(token);
        }
    }

    /**
     * Resolves a session token to a customer ID.
     */
    public Integer getCustomerIdByToken(String token) {
        if (token == null) {
            return null;
        }
        return activeSessions.get(token);
    }

    /**
     * Resolves a session token to a full Customer model object.
     */
    public Customer getCustomerByToken(String token) {
        Integer customerId = getCustomerIdByToken(token);
        if (customerId == null) {
            return null;
        }
        return customerDAO.findById(customerId);
    }
}
