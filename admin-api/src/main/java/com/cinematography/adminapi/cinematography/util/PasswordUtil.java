package com.cinematography.adminapi.cinematography.util;

import com.cinematography.adminapi.cinematography.util.EncryptionService;

public class PasswordUtil {
    public static void main(String[] args) {
        String password = "AnantShreeVani@2026!";
        String encodedPassword = EncryptionService.encrypt(password, "wzebxmci9caz3lyy9sdllj9l5ipsxc");
        System.out.println("Encoded Password: " + encodedPassword);
    }
}
