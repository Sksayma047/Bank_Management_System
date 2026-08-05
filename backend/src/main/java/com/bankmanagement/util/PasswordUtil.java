package com.bankmanagement.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    
    /**
     * Hashes a plain-text password using BCrypt.
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Checks if a plain-text password matches a BCrypt hash.
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
