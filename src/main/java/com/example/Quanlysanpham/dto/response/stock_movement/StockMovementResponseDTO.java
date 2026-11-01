package com.example.Quanlysanpham.dto.response.stock_movement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponseDTO {
    private int id;
    private String type;
    private int quantity;
    private String note;
    private LocalDateTime createdAt;
    private int createdBy;
}
