package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.user.UserLoginRequestDTO;
import com.example.Quanlysanpham.dto.request.user.UserRegisterRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // Đăng ký tài khoản
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody UserRegisterRequestDTO request) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Tạo tk thành công",
                authService.register(request)

        ));
    }

    // Đăng nhập
    public ResponseEntity<ApiResponse<UserResponseDTO>> login(@Valid @RequestBody UserLoginRequestDTO request) {

    }
}
