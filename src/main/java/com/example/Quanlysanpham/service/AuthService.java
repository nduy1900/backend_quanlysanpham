package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.user.UserLoginRequestDTO;
import com.example.Quanlysanpham.dto.request.user.UserRegisterRequestDTO;
import com.example.Quanlysanpham.dto.response.user.UserLoginResponseDTO;
import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.mapper.UserResponseMapper;
import com.example.Quanlysanpham.repository.UserRepository;
import com.example.Quanlysanpham.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserResponseMapper userMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    // Đăng ký
    public UserResponseDTO register(UserRegisterRequestDTO request) {
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

        // Sinh accessToken
        String accessToken = jwtService.generateAccessToken(userDetails);

        UserLoginResponseDTO response = new UserLoginResponseDTO();
        response.setAccessToken(accessToken);
        return response;
    }
}
