package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.product.ProductRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@EnableMethodSecurity
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProductPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh sách sản phẩm thành công",
                productService.getPage(page, size)
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getId(@PathVariable int id) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập sản phẩm có id = " + id,
                productService.getProductById(id)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> postProduct(
            @Valid @RequestBody ProductRequestDTO request
            , Authentication authentication) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Tạo sản phẩm thành công",
                productService.createProduct(request, authentication)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> putProduct(
            @PathVariable int id,
            @Valid @RequestBody ProductRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Cập nhật sản phẩm thành công",
                productService.updateProduct(id, request, authentication)
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable int id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Xoá sản phẩm thành công",
                null
        ));
    }
}
