package com.example.Quanlysanpham.dto.request.product_supplier;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplierRequestDTO {
    @NotNull(message = "Giá nhập không được để trống")
    @PositiveOrZero(message = "Giá nhập phải lớn hơn hoặc bằng 0")
    private double supplyPrice;


    @NotNull(message = "Nhà cung cấp không được bỏ trống")
    @Positive(message = "Supplier ID phải lớn hơn 0")
    private int supplierId;

}
