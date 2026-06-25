package com.kavya.stealthpad.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kavya.stealthpad.Entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class AuthUtils {

    @Value("${jwt.secret}")
    private String JWT_SECRET_KEY;

    public SecretKey getSecretKey() {

        return Keys.hmacShaKeyFor(
                JWT_SECRET_KEY.getBytes(
                        StandardCharsets.UTF_8
                )
        );
    }

    public String generateToken(User user){
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) //  30 days valid access token...
                .signWith(getSecretKey()).compact();
    }

    
}