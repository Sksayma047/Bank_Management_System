package com.bankmanagement;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import com.bankmanagement.controller.*;

public class App {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.err.println("Invalid PORT env variable. Falling back to 8080.");
            }
        }

        Server server = new Server(port);

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        server.setHandler(handler);

        // 1. Configure and add CORS filter
        FilterHolder corsFilterHolder = new FilterHolder(new CorsFilter());
        handler.addFilter(corsFilterHolder, "/api/*", EnumSet.of(DispatcherType.REQUEST));

        // 2. Configure and add JWT/Session Auth Filter
        FilterHolder authFilterHolder = new FilterHolder(new AuthFilter());
        handler.addFilter(authFilterHolder, "/api/*", EnumSet.of(DispatcherType.REQUEST));

        // 3. Register Servlets
        handler.addServlet(new ServletHolder(new AuthController()), "/api/auth/*");
        handler.addServlet(new ServletHolder(new CustomerController()), "/api/customers/*");
        handler.addServlet(new ServletHolder(new AccountController()), "/api/accounts/*");
        handler.addServlet(new ServletHolder(new TransactionController()), "/api/transactions/*");

        System.out.println("============================================================");
        System.out.println("  Bank Management System (Jetty + Jakarta Servlets)         ");
        System.out.println("  Running on: http://localhost:" + port                       );
        System.out.println("============================================================");

        server.start();
        server.join();
    }
}
