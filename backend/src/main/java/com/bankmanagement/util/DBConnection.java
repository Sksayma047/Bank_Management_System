package com.bankmanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.bankmanagement.config.AppConfig;

public class DBConnection {
    static {
        try {
            // Explicitly load the MySQL JDBC driver for demonstration purposes
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    private static Connection testConnection = null;

    /**
     * Injects a test connection to bypass the DriverManager during tests.
     */
    public static void setTestConnection(Connection conn) {
        testConnection = conn;
    }

    /**
     * Obtains a new connection to the MySQL database or returns the injected test connection.
     * Callers must close this connection (e.g., using try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        if (testConnection != null) {
            return testConnection;
        }
        return DriverManager.getConnection(
            AppConfig.DB_URL,
            AppConfig.DB_USER,
            AppConfig.DB_PASSWORD
        );
    }
}
