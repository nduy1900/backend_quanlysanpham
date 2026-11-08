package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.category.CategoryRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.category.CategoryResponseDTO;
import com.example.Quanlysanpham.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@EnableMethodSecurity
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getCategoryList() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh sách danh mục sản phẩm thành công",
                categoryService.getAll()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategoryById(@PathVariable int id) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh mục sản phẩm có id = " + id,
                categoryService.getCategoryById(id)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> postCategory(@Valid @RequestBody CategoryRequestDTO request, Authentication authentication) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Tạo danh mục sản phẩm thành công",
                categoryService.createCategory(request, authentication)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> putCategory(@PathVariable int id, @Valid @RequestBody CategoryRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Cập nhật danh mục sản phẩm thành công",
                categoryService.updateCategory(id, request, authentication)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable int id, Authentication authentication) {
        categoryService.deleteCategory(id, authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Xoá danh mục sản phẩm thành công",
                null
        ));
    }

}
