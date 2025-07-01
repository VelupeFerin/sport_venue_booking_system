package com.sport_venue_booking_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {
    
    @Value("${jwt.expiration:86400000}") // 默认24小时
    private long expirationTime;
    
    @Value("${jwt.secret}")
    private String secretString;
    
    private SecretKey getSecretKey() {
        // 确保secret只包含ASCII字符
        byte[] secretBytes = secretString.getBytes(StandardCharsets.US_ASCII);
        System.out.println("Secret key length: " + secretBytes.length);
        System.out.println("Secret key (first 20 chars): " + secretString.substring(0, Math.min(20, secretString.length())));
        return Keys.hmacShaKeyFor(secretBytes);
    }
    
    public String generateToken(String username) {
        System.out.println("Generating token for username: " + username);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        // 使用Base64编码用户名，避免中文字符在HTTP请求头中的编码问题
        String encodedUsername = Base64.getEncoder().encodeToString(username.getBytes(StandardCharsets.UTF_8));
        System.out.println("Encoded username: " + encodedUsername);
        
        String token = Jwts.builder()
                .setSubject(encodedUsername)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
        
        System.out.println("Generated token (first 50 chars): " + token.substring(0, Math.min(50, token.length())));
        
        // 验证生成的token只包含安全字符
        validateTokenCharacters(token);
        
        return token;
    }
    
    public String getUsernameFromToken(String token) {
        System.out.println("Extracting username from token (first 50 chars): " + token.substring(0, Math.min(50, token.length())));
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        // 解码Base64编码的用户名
        String encodedUsername = claims.getSubject();
        System.out.println("Encoded username from token: " + encodedUsername);
        
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedUsername);
            String decodedUsername = new String(decodedBytes, StandardCharsets.UTF_8);
            System.out.println("Decoded username: " + decodedUsername);
            return decodedUsername;
        } catch (Exception e) {
            // 如果解码失败，可能是旧版本的token，直接返回原始值
            System.out.println("Failed to decode username, returning original: " + encodedUsername);
            return encodedUsername;
        }
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证token只包含安全的ASCII字符
     */
    private void validateTokenCharacters(String token) {
        System.out.println("Validating token characters, length: " + token.length());
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c > 127) {
                String error = "Token contains non-ASCII character at position " + i + ": " + c + " (code: " + (int)c + ")";
                System.err.println(error);
                throw new RuntimeException(error);
            }
            // JWT token应该只包含字母、数字、-、_、.、=
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '_' && c != '.' && c != '=') {
                String error = "Token contains invalid character at position " + i + ": " + c + " (code: " + (int)c + ")";
                System.err.println(error);
                throw new RuntimeException(error);
            }
        }
        System.out.println("Token character validation passed");
    }
} 