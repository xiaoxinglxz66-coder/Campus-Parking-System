package com.example.campusparkingbackend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    // 修复密钥生成方法
    private Key getSigningKey() {
        System.out.println("=== JwtUtil 获取签名密钥 ===");
        System.out.println("配置的密钥: " + secret);

        // 检查密钥是否是 Base64 编码的
        if (secret.length() >= 32) {
            try {
                // 尝试作为 Base64 解码
                byte[] keyBytes = Decoders.BASE64.decode(secret);
                System.out.println("使用 Base64 解码的密钥");
                return Keys.hmacShaKeyFor(keyBytes);
            } catch (Exception e) {
                System.out.println("Base64 解码失败，使用原始字符串");
            }
        }

        // 使用原始字符串作为密钥
        System.out.println("使用原始字符串作为密钥");
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String role, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId) // ✅ 添加 userId
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 添加获取 userId 的方法
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", Long.class);
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        try {
            System.out.println("🔍 JwtUtil.getRoleFromToken() 开始");
            System.out.println("🔑 输入Token长度: " + (token != null ? token.length() : 0));
            System.out.println("🔑 输入Token前30字符: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "null"));

            // 检查token是否有效
            if (token == null || token.trim().isEmpty()) {
                System.err.println("❌ Token为空或空白");
                return null;
            }

            // 验证Token格式
            if (token.split("\\.").length != 3) {
                System.err.println("❌ Token格式错误，部分数: " + token.split("\\.").length);
                return null;
            }

            System.out.println("🔄 开始解析Token...");

            // 解析Token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("✅ Token解析成功");

            // 获取role字段
            String role = claims.get("role", String.class);
            System.out.println("👤 提取的role字段: " + role);

            // 打印所有claims用于调试
            System.out.println("📋 所有Claims:");
            claims.forEach((key, value) -> {
                System.out.println("   " + key + ": " + value);
            });

            return role;

        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.err.println("❌ Token签名无效");
            System.err.println("   错误信息: " + e.getMessage());
            System.err.println("   可能原因: 签名密钥不匹配");
            return null;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println("❌ Token已过期");
            System.err.println("   过期时间: " + e.getClaims().getExpiration());
            return null;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.err.println("❌ Token格式错误");
            System.err.println("   错误信息: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("❌ 解析Token时发生未知异常");
            System.err.println("   异常类型: " + e.getClass().getName());
            System.err.println("   错误信息: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean validateToken(String token) {
        System.out.println("=== JwtUtil 验证 Token ===");
        System.out.println("待验证Token: " + token);

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            System.out.println("✅ Token 验证成功");
            return true;
        } catch (Exception e) {
            System.out.println("❌ Token 验证失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
