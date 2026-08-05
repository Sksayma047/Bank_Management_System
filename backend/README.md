# Bank Management System - Backend API

This directory contains the Java REST API backend for the Bank Management System, built using Core Java, Jakarta Servlets, raw JDBC, and embedded Eclipse Jetty.

## Key Technologies
- **JDK 17+**
- **Jakarta Servlets 5.0**
- **Eclipse Jetty 11.0.15** (Embedded Servlet Container)
- **MySQL Connector/J 8.0.33**
- **Google Gson 2.10.1** (JSON mappings)
- **BCrypt 0.4** (Password Hashing)
- **JUnit 5.10.0** (Testing)

## Getting Started
1. Configure your database settings inside `src/main/java/com/bankmanagement/config/AppConfig.java` or via environment variables (`DB_USER`, `DB_PASSWORD`).
2. Run unit tests to verify:
   ```bash
   mvn test
   ```
3. Compile and start the server:
   ```bash
   mvn compile exec:java
   ```
   *The server starts on port `8080`.*

For full details, see the main [README.md](../README.md) in the workspace root.
