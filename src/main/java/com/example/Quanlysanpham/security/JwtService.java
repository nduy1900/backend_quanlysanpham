package com.example.Quanlysanpham.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    // Sinh secretKey
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );
    }

    // Tạo JWT TOKEN (ACCESS TOKEN)
    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                // thông tin người dùng
                .subject(userDetails.getUsername())

                // custom claim
                // lấy role
                .claim(
                        "role", userDetails.getAuthorities().stream().findFirst()
                                .map(GrantedAuthority::getAuthority).orElse("")
                )
                .claim("type", "access")
                // ngày tạo
                .issuedAt(new Date())
                // ngày hết hạn
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey())
                .compact();
    }
}
