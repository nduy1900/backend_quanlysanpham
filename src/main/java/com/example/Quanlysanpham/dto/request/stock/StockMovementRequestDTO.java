package com.example.Quanlysanpham.dto.request.stock;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementRequestDTO {

    @NotNull(message = "Số lượng không được bỏ trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private int quantity;

    private String note;

    @NotNull(message = "Product ID không được bỏ trống")
    @Positive(message = "Product ID không được âm")
    private int productId;
}
