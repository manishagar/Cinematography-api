package com.cinematography.adminapi.cinematography.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecurityKeyGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

        public static void main(String[] args) {

        // Salt compatible with your existing EncryptionService
        String salt = generateSalt(32);

        // 512-bit JWT secret for HS512
        byte[] jwtKeyBytes = new byte[64];
        RANDOM.nextBytes(jwtKeyBytes);

        String jwtSecretKey = Base64.getEncoder()
                .encodeToString(jwtKeyBytes);

        System.out.println("SECURITY_SALT=" + salt);
        System.out.println("JWT_SECRET_KEY=" + jwtSecretKey);
    }

    private static String generateSalt(int length) {

        String alphabet =
                "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        StringBuilder salt = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            salt.append(alphabet.charAt(
                    RANDOM.nextInt(alphabet.length())));
        }

        return salt.toString();
    }
}
