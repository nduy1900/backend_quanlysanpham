package com.example.Quanlysanpham.dto.request.brand;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequestDTO {
    @NotBlank(message = "Tên thương hiệu không đợc bỏ trống")
    private String name;

    @NotBlank(message = "Tên quốc gia không được bỏ trống")
    private String country;
}
