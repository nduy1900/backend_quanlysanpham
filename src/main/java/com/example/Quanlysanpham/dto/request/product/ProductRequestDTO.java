package com.example.Quanlysanpham.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    @NotBlank(message = "SKU không được bỏ trống")
    private String sku;

    @NotBlank(message = "Tên sản phẩm không được bỏ trống")
    private String name;

    @NotNull(message = "Giá sản phẩm không được để trống")
    @PositiveOrZero(message = "Giá phải lớn hơn hoặc bằng 0")
    private double price;

    @NotNull(message = "Số lượng không được để trống")
    @PositiveOrZero(message = "Số lượng phải lớn hơn hoặc bằng 0")
    private int quantity;

    @NotNull(message = "Số lượng tồn kho tối thiểu không được để trống")
    @PositiveOrZero(message = "Số lượng tồn kho tối thiểu không hợp lệ")
    private int minStock;

    private String status;
    
    @NotNull(message = "Category ID không được để trống")
    private int categoryId;

    @NotNull(message = "Brand ID không được để trống")
    private int brandId;
}
