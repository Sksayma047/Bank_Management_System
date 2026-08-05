package com.bankmanagement.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import com.bankmanagement.exception.CustomerNotFoundException;
import com.bankmanagement.model.Customer;
import com.bankmanagement.service.CustomerService;

public class CustomerController extends BaseController {
    private final CustomerService customerService;

    public CustomerController() {
        this.customerService = new CustomerService();
    }

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int pathId = getIdFromPath(req);
        Integer tokenCustomerId = (Integer) req.getAttribute("customerId");

        // If path is empty, fetch the logged-in customer profile
        int targetId = (pathId == -1) ? tokenCustomerId : pathId;

        // Security check: Customer can only access their own profile
        if (tokenCustomerId != targetId) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You cannot view this profile");
            return;
        }

        try {
            Customer customer = customerService.getCustomerById(targetId);
            // Redact password hash for safety
            customer.setPasswordHash(null);
            writeJson(resp, HttpServletResponse.SC_OK, customer);
        } catch (CustomerNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred retrieving profile");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Public endpoint for customer registration
        try {
            RegistrationRequest regReq = readJson(req, RegistrationRequest.class);
            if (regReq == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload");
                return;
            }

            Customer customer = new Customer();
            customer.setFullName(regReq.getFullName());
            customer.setEmail(regReq.getEmail());
            customer.setPhone(regReq.getPhone());
            customer.setAddress(regReq.getAddress());
            if (regReq.getDateOfBirth() != null) {
                customer.setDateOfBirth(LocalDate.parse(regReq.getDateOfBirth()));
            }

            Customer saved = customerService.registerCustomer(customer, regReq.getPassword());
            saved.setPasswordHash(null); // redact
            
            writeJson(resp, HttpServletResponse.SC_CREATED, saved);
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int pathId = getIdFromPath(req);
        Integer tokenCustomerId = (Integer) req.getAttribute("customerId");

        int targetId = (pathId == -1) ? tokenCustomerId : pathId;

        // Security check: Customer can only update their own profile
        if (tokenCustomerId != targetId) {
            writeError(resp, HttpServletResponse.SC_FORBIDDEN, "Access Denied: You cannot edit this profile");
            return;
        }

        try {
            Customer updateData = readJson(req, Customer.class);
            if (updateData == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request payload");
                return;
            }

            Customer updated = customerService.updateCustomer(targetId, updateData);
            updated.setPasswordHash(null); // redact
            
            writeJson(resp, HttpServletResponse.SC_OK, updated);
        } catch (CustomerNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // Input Request DTO helper class
    private static class RegistrationRequest {
        private String fullName;
        private String email;
        private String phone;
        private String address;
        private String dateOfBirth;
        private String password;

        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public String getDateOfBirth() { return dateOfBirth; }
        public String getPassword() { return password; }
    }
}
