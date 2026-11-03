package com.example.Quanlysanpham.dto.request.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequestDTO {
    @NotBlank(message = "Tên nhà cung cấp không được bỏ trống")
    private String name;

    @NotBlank(message = "Số điện thoại nhà cung cấp không được bỏ trống")
    private String phone;

    @Email
    private String email;

    @NotBlank(message = "Địa chỉ nhà cung cấp không được bỏ trống")
    private String address;

}
