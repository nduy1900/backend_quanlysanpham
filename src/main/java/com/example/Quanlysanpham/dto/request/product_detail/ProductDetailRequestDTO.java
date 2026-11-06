package com.example.Quanlysanpham.dto.request.product_detail;

import jakarta.validation.constraints.NotBlank;
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
public class ProductDetailRequestDTO {
    @NotBlank(message = "Mô tả sản phẩm không được bỏ trống")
    private String description;

    @NotNull(message = "Cân nặng sản phẩm không được bỏ trống")
    @Positive(message = "Cân nặng sản phẩm không được < 0")
    private double weight;

    @NotBlank(message = "Kích thước sản phẩm không được bỏ trống")
    private String dimensions;

    @NotBlank(message = "Màu sắc sản phẩm không được bỏ trống")
    private String color;

    @NotNull(message = "Bảo hành sản phẩm không được bỏ trống")
    @Positive(message = "Bảo hành sản phẩm không được < 0")
    private int warrantyMonths;
    
}
