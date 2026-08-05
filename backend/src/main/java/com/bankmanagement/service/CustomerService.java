package com.bankmanagement.service;

import java.time.LocalDateTime;
import com.bankmanagement.dao.CustomerDAO;
import com.bankmanagement.exception.CustomerNotFoundException;
import com.bankmanagement.model.Customer;
import com.bankmanagement.util.PasswordUtil;
import com.bankmanagement.util.ValidationUtil;

public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    /**
     * Registers a new customer in the system.
     */
    public Customer registerCustomer(Customer customer, String plainPassword) throws Exception {
        // Validation checks
        if (customer == null) {
            throw new IllegalArgumentException("Customer object cannot be null");
        }
        if (!ValidationUtil.isNotEmpty(customer.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (!ValidationUtil.isValidEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!ValidationUtil.isValidPhone(customer.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (!ValidationUtil.isNotEmpty(customer.getAddress())) {
            throw new IllegalArgumentException("Address is required");
        }
        if (customer.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Date of birth is required");
        }
        if (!ValidationUtil.isValidPassword(plainPassword)) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        // Duplicate checks
        if (customerDAO.findByEmail(customer.getEmail()) != null) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (customerDAO.findByPhone(customer.getPhone()) != null) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        // Setup password hash and timestamps
        customer.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        customer.setCreatedAt(LocalDateTime.now());

        Customer saved = customerDAO.save(customer);
        if (saved == null) {
            throw new Exception("Customer registration failed");
        }
        return saved;
    }

    /**
     * Retrieves customer details by ID.
     */
    public Customer getCustomerById(int id) throws CustomerNotFoundException {
        Customer c = customerDAO.findById(id);
        if (c == null) {
            throw new CustomerNotFoundException("Customer with ID " + id + " not found");
        }
        return c;
    }

    /**
     * Updates customer profile details.
     */
    public Customer updateCustomer(int id, Customer updateData) throws CustomerNotFoundException, Exception {
        Customer existing = customerDAO.findById(id);
        if (existing == null) {
            throw new CustomerNotFoundException("Customer with ID " + id + " not found");
        }

        // Validation checks
        if (!ValidationUtil.isNotEmpty(updateData.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (!ValidationUtil.isValidEmail(updateData.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!ValidationUtil.isValidPhone(updateData.getPhone())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (!ValidationUtil.isNotEmpty(updateData.getAddress())) {
            throw new IllegalArgumentException("Address is required");
        }

        // Check duplicates if email/phone changed
        if (!existing.getEmail().equalsIgnoreCase(updateData.getEmail())) {
            if (customerDAO.findByEmail(updateData.getEmail()) != null) {
                throw new IllegalArgumentException("Email already taken");
            }
        }
        if (!existing.getPhone().equals(updateData.getPhone())) {
            if (customerDAO.findByPhone(updateData.getPhone()) != null) {
                throw new IllegalArgumentException("Phone number already taken");
            }
        }

        existing.setFullName(updateData.getFullName());
        existing.setEmail(updateData.getEmail());
        existing.setPhone(updateData.getPhone());
        existing.setAddress(updateData.getAddress());
        if (updateData.getDateOfBirth() != null) {
            existing.setDateOfBirth(updateData.getDateOfBirth());
        }

        boolean success = customerDAO.update(existing);
        if (!success) {
            throw new Exception("Failed to update customer profile");
        }
        return existing;
    }
}
