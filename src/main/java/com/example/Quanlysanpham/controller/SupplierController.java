package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.supplier.SupplierRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.supplier.SupplierResponseDTO;
import com.example.Quanlysanpham.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@EnableMethodSecurity
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierResponseDTO>>> getList() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh sách các nhà cung cấp",
                supplierService.getAll()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> getSupplierById(@PathVariable int id) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập nhà cung cấp thứ " + id,
                supplierService.getById(id)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> postSupplier(@Valid @RequestBody SupplierRequestDTO request) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Tạo nhà cung cấp thành công",
                supplierService.createSupplier(request)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierResponseDTO>> putSupplier(@PathVariable int id, @Valid @RequestBody SupplierRequestDTO request) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Cập nhật nhà cung cấp thành công",
                supplierService.updateSupplier(id, request)
        ));
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable int id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Xoá nhà cung cấp thành công",
                null
        ));
    }

}
