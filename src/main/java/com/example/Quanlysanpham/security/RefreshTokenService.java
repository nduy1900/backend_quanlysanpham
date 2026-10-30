package com.example.Quanlysanpham.security;

import com.example.Quanlysanpham.entity.RefreshToken;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.repository.RefreshTokenRepository;
import com.example.Quanlysanpham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.expiration-refresh}")
    private long expirationRefresh;


    public RefreshToken save(String token, String username, String family_id) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy user")
        );

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUsername(username);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(expirationRefresh / 1000));
        refreshToken.setFamilyId(family_id);
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    // Tìm Refresh Token
    public RefreshToken findByToken(String tokenRefresh) {
        return refreshTokenRepository.findByToken(tokenRefresh)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy refresh token"));
    }

    // Verify Token (kiểm tra thời gian sống của Refresh Token)
    public void verifyToken(RefreshToken refreshToken) {
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("Refresh Token đã hết hạn");
        }
    }

    // Vô hiệu hoá Refresh Token
    public RefreshToken revoke(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
        return refreshTokenRepository.save(refreshToken);
    }

    // Vô hiệu hoá toàn bộ token trong family
    public void revokeFamily(String family_id) {
        List<RefreshToken> tokens = refreshTokenRepository.findByFamilyId(family_id);

        tokens.forEach(token -> token.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }
}
