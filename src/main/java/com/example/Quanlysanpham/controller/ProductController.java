package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getProductList() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Xem danh sách sản phẩm",
                productService.getAll()
        ));
    }
}
