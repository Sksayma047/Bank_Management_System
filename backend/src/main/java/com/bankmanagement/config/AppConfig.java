package com.bankmanagement.config;

public class AppConfig {
    public static final String DB_URL = getEnv("DB_URL",
            "jdbc:mysql://localhost:3306/bank_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    public static final String DB_USER = getEnv("DB_USER", "root");
    public static final String DB_PASSWORD = getEnv("DB_PASSWORD", "Sayma@123");

    private static String getEnv(String name, String defaultValue) {
        String val = System.getenv(name);
        return val != null ? val : defaultValue;
    }
}
