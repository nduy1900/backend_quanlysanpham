package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.category.CategoryReportResponseDTO;
import com.example.Quanlysanpham.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@EnableMethodSecurity
public class ReportController {
    private final ReportService reportService;

    // LẤY SỐ SẢN PHẨM THEO Category
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/by-category")
    public ResponseEntity<ApiResponse<List<CategoryReportResponseDTO>>> getProductByCategory() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Thống kê số lượng sản phẩm theo danh mục thành công",
                reportService.getProductByCategory()
        ));
    }
}
