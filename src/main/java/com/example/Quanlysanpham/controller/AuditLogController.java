package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.entity.AuditLog;
import com.example.Quanlysanpham.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@EnableMethodSecurity
public class AuditLogController {
    private final AuditLogService auditLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAllLogs() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập lịch sử hoạt động thành công",
                auditLogService.getAllLogs()
        ));
    }
}
