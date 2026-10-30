package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@EnableMethodSecurity
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getUsers() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh sách người dùng thành công!",
                userService.getAllUsers()
        ));
    }
}
