package com.cinematography.adminapi.cinematography.util;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;

public class JwtSecretConverter {
    public static void main(String[] args) {
     SecretKey key = Jwts.SIG.HS512.key().build();
     String secretKey = Encoders.BASE64.encode(key.getEncoded());

        System.out.println("JWT Secret Key: " + secretKey);
    }
}
