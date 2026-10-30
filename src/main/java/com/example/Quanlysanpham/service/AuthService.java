package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.refresh.RefreshTokenRequestDTO;
import com.example.Quanlysanpham.dto.request.user.UserLoginRequestDTO;
import com.example.Quanlysanpham.dto.request.user.UserRegisterRequestDTO;
import com.example.Quanlysanpham.dto.response.user.UserLoginResponseDTO;
import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.entity.RefreshToken;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.mapper.UserResponseMapper;
import com.example.Quanlysanpham.repository.UserRepository;
import com.example.Quanlysanpham.security.CustomUserDetailsService;
import com.example.Quanlysanpham.security.JwtService;
import com.example.Quanlysanpham.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserResponseMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;


    // Đăng ký
    public UserResponseDTO register(UserRegisterRequestDTO request) {
        // Kiểm tra tồn tại username hoặc email
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Dữ liệu đã tồn tại");
        }
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setFullName(request.getFullName());
        newUser.setEnabled(true);
        newUser.setCreatedAt(LocalDateTime.now());

        //password phải hash
        String hashPassword = passwordEncoder.encode(request.getPassword());
        newUser.setPassword(hashPassword);


        userRepository.save(newUser);
        return userMapper.toResponse(newUser);
    }

    // Đăng nhập
    public UserLoginResponseDTO login(UserLoginRequestDTO request) {
        // kiểm tra username, password ~ tìm user, map mật khẩu
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Lấy thông tin người dùng đã xác thực
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Sinh accessToken, refreshToken
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // tạo token family (family_id chỉ được lưu duy nhất 1 lần khi login)
        String familyId = UUID.randomUUID().toString();

        // lưu refreshToken vào DB khi đăng nhập
        refreshTokenService.save(refreshToken, userDetails.getUsername(), familyId);

        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        return response;
    }

    // Dùng refreshToken tạo accessToken mới
    public UserLoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        // 1. Lấy Refresh Token từ request
        String token = request.getRefreshToken();

        // 2. Tìm Refresh Token trong DB
        RefreshToken refreshTokenInDB = refreshTokenService.findByToken(token);

        // 3. Kiểm tra Refresh Token đã revoke chưa, kiểm tra reuse
        if (refreshTokenInDB.isRevoked()) {
            String familyId = refreshTokenInDB.getFamilyId();

            // Reuse Detected -> revoke toàn bộ family
            refreshTokenService.revokeFamily(familyId);

            throw new RuntimeException("Refresh Token reuse detected");
        }

        // 4. Kiểm tra xem Refresh Token còn hạn hay không
        refreshTokenService.verifyToken(refreshTokenInDB);

        // 5. Lấy username
        String username = refreshTokenInDB.getUsername();

        // 6. Lấy userDetails
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // 7. Tạo AccessToken mới
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        // 8. Tạo RefreshToken mới và lưu trong DB
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // 9. Vô hiệu hoá Refresh Token trong DB
        refreshTokenService.revoke(refreshTokenInDB);

        // 10. Lưu Refresh Token mới
        String familyId = refreshTokenInDB.getFamilyId();
        refreshTokenService.save(newRefreshToken, userDetails.getUsername(), familyId);

        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        return response;
    }

    // Đăng xuất
    public void logout(RefreshTokenRequestDTO request) {
        // Lấy Refresh Token từ request
        String token = request.getRefreshToken();

        // Tìm Refresh Token trong DB
        RefreshToken refreshTokenInDB = refreshTokenService.findByToken(token);

        // Lấy family_id
        String family_id = refreshTokenInDB.getFamilyId();

        // Revoke toàn bộ family
        refreshTokenService.revokeFamily(family_id);
    }
}
