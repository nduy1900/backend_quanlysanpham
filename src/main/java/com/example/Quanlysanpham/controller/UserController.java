package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.user.UpdateUserRoleRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.user.UserResponseDTO;
import com.example.Quanlysanpham.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

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

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<String>> updateRoleManagementAccount(@PathVariable int id, @Valid @RequestBody UpdateUserRoleRequestDTO request) {
        userService.updateUser(id, request);
        return ResponseEntity.ok(new ApiResponse<>(
                        200,
                        "Cập nhật người dùng thành công",
                        "Người dùng có id = " + id + "đã cập nhật"
                )
        );
    }

}
