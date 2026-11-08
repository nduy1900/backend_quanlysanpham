package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.brand.BrandRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.brand.BrandResponseDTO;
import com.example.Quanlysanpham.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@EnableMethodSecurity
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponseDTO>>> getBrandList() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh sách thương hiệu sản phẩm thành công",
                brandService.getAll()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> getBrandById(@PathVariable int id) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập thương hiệu có id = " + id,
                brandService.getBrandById(id)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<BrandResponseDTO>> postBrand(@Valid @RequestBody BrandRequestDTO request, Authentication authentication) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Tạo thương hiệu sản phẩm thành công",
                brandService.createBrand(request, authentication)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponseDTO>> putBrand(@PathVariable int id, @Valid @RequestBody BrandRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Cập nhật thương hiệu sản phẩm thành công",
                brandService.updateBrand(id, request, authentication)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBrand(@PathVariable int id, Authentication authentication) {
        brandService.deleteBrand(id, authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Xoá thương hiệu sản phẩm thành công",
                null
        ));
    }
}
