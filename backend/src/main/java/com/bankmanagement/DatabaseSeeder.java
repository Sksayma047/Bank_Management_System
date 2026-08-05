package com.bankmanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import com.bankmanagement.util.DBConnection;

public class DatabaseSeeder {
    public static void main(String[] args) {
        String schemaPath = "../database/schema.sql";
        String sampleDataPath = "../database/sample-data.sql";

        System.out.println("Initializing Database Seeder...");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Successfully connected to MySQL database!");

            // 1. Run Schema
            System.out.println("Running schema.sql...");
            executeSqlFile(stmt, schemaPath);
            System.out.println("schema.sql executed successfully!");

            // 2. Run Sample Data
            System.out.println("Running sample-data.sql...");
            executeSqlFile(stmt, sampleDataPath);
            System.out.println("sample-data.sql executed successfully!");

            System.out.println("Database setup completed successfully!");
        } catch (Exception e) {
            System.err.println("Error seeding database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void executeSqlFile(Statement stmt, String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().startsWith("--") || line.trim().startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                sb.append(line).append(" ");
            }
        }

        // Split by semicolon, making sure we don't break inside quotes (for simplicity, we split by semicolon)
        String[] queries = sb.toString().split(";");
        for (String query : queries) {
            if (query.trim().isEmpty()) {
                continue;
            }
            try {
                stmt.execute(query.trim());
            } catch (Exception e) {
                System.err.println("Failed to execute query: " + query.trim());
                throw e;
            }
        }
    }
}
