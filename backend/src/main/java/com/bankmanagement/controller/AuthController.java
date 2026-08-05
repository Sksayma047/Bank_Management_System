package com.bankmanagement.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.bankmanagement.exception.AuthenticationException;
import com.bankmanagement.model.Customer;
import com.bankmanagement.service.AuthService;
import com.bankmanagement.service.CustomerService;

public class AuthController extends BaseController {
    private final AuthService authService;
    private final CustomerService customerService;

    public AuthController() {
        this.authService = new AuthService();
        this.customerService = new CustomerService();
    }

    public AuthController(AuthService authService, CustomerService customerService) {
        this.authService = authService;
        this.customerService = customerService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid authentication endpoint");
            return;
        }

        if (pathInfo.equals("/login")) {
            handleLogin(req, resp);
        } else if (pathInfo.equals("/logout")) {
            handleLogout(req, resp);
        } else {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Authentication action not found");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            LoginRequest loginReq = readJson(req, LoginRequest.class);
            if (loginReq == null || loginReq.getEmail() == null || loginReq.getPassword() == null) {
                writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required");
                return;
            }

            String token = authService.login(loginReq.getEmail(), loginReq.getPassword());
            Integer customerId = authService.getCustomerIdByToken(token);
            Customer customer = customerService.getCustomerById(customerId);

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("token", token);
            data.put("customerId", customer.getCustomerId());
            data.put("fullName", customer.getFullName());
            data.put("email", customer.getEmail());

            writeJson(resp, HttpServletResponse.SC_OK, data);
        } catch (AuthenticationException e) {
            writeError(resp, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred during login");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        responseMap.put("message", "Logged out successfully");
        writeJson(resp, HttpServletResponse.SC_OK, responseMap);
    }

    // Helper requests model
    private static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
