package com.bankmanagement.service;

import org.junit.jupiter.api.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.Statement;
import com.bankmanagement.util.DBConnection;

public class DatabaseSeederTest {

    @Test
    public void seedDatabase() {
        String schemaPath = "../database/schema.sql";
        String sampleDataPath = "../database/sample-data.sql";

        System.out.println(">>> RUNNING DATABASE SEEDER TEST SYSTEM <<<");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected to MySQL successfully!");

            // Execute schema
            System.out.println("Executing schema.sql...");
            executeSqlFile(stmt, schemaPath);

            // Execute sample data
            System.out.println("Executing sample-data.sql...");
            executeSqlFile(stmt, sampleDataPath);

            System.out.println("Database tables and test users loaded successfully!");
        } catch (Exception e) {
            System.err.println("Database seeding failed: " + e.getMessage());
            e.printStackTrace();
            org.junit.jupiter.api.Assertions.fail(e);
        }
    }

    private void executeSqlFile(Statement stmt, String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("--") || line.trim().startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                sb.append(line).append(" ");
            }
        }

        String[] queries = sb.toString().split(";");
        for (String query : queries) {
            if (query.trim().isEmpty()) {
                continue;
            }
            stmt.execute(query.trim());
        }
    }
}
