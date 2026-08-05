package com.bankmanagement.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import com.bankmanagement.service.AuthService;
import com.google.gson.Gson;

public class AuthFilter implements Filter {
    private final AuthService authService;
    private final Gson gson = new Gson();

    public AuthFilter() {
        this.authService = new AuthService();
    }

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        // 1. Bypass authentication check for Login and Customer Registration
        if (path.endsWith("/api/auth/login") || (path.endsWith("/api/customers") && "POST".equalsIgnoreCase(method))) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Extract Authorization Header
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedError(resp, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        Integer customerId = authService.getCustomerIdByToken(token);

        if (customerId == null) {
            sendUnauthorizedError(resp, "Session expired or invalid token");
            return;
        }

        // 3. Attach customer ID to request attributes for controllers
        req.setAttribute("customerId", customerId);
        chain.doFilter(request, response);
    }

    private void sendUnauthorizedError(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        BaseController.ErrorResponse err = new BaseController.ErrorResponse(message);
        resp.getWriter().write(gson.toJson(err));
    }

    @Override
    public void destroy() {}
}
