package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.stock.StockMovementRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.stock_movement.StockMovementHistoryResponseDTO;
import com.example.Quanlysanpham.dto.response.stock_movement.StockMovementResponseDTO;
import com.example.Quanlysanpham.service.StockMovementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockMovementController {
    private final StockMovementService stockMovementService;

    // NHẬP KHO
    @PostMapping("/in")
    public ResponseEntity<ApiResponse<StockMovementResponseDTO>> importProduct(
            @Valid @RequestBody StockMovementRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Nhập kho sản phẩm thành công",
                stockMovementService.importProduct(request, authentication)
        ));

    }

    // XUẤT KHO
    @PostMapping("/out")
    public ResponseEntity<ApiResponse<StockMovementResponseDTO>> exportProduct(
            @Valid @RequestBody StockMovementRequestDTO request,
            Authentication authentication) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Xuất kho sản phẩm thành công",
                stockMovementService.exportProduct(request, authentication)
        ));
    }

    // LỊCH SỬ NHẬP XUẤT KHO
    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<List<StockMovementHistoryResponseDTO>>> getStockMovements(@RequestParam String type) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập lịch sử nhập/xuất thành công",
                stockMovementService.getStockMovements(type)
        ));
    }

}
