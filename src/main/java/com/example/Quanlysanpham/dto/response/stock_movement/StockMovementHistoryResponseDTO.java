package com.example.Quanlysanpham.dto.response.stock_movement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StockMovementHistoryResponseDTO {
    private int id;

    private String type;

    private int quantity;

    private String note;

    private LocalDateTime createdAt;

    private Integer createdBy;

    private Integer productId;

    private String productName;
}
