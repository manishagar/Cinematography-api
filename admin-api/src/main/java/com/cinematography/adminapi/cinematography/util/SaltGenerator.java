package com.cinematography.adminapi.cinematography.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SaltGenerator {
    public static void main(String[] args) {
                byte[] saltBytes = new byte[32]; // 256 bits

        SecureRandom random = new SecureRandom();
        random.nextBytes(saltBytes);

        String salt = Base64.getEncoder()
                .encodeToString(saltBytes);

        System.out.println("Generated Salt: " + salt);
    }
}
