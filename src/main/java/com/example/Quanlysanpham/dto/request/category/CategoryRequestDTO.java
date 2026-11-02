package com.example.Quanlysanpham.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {
    @NotBlank(message = "Tên danh mục không được bỏ trống")
    private String name;

    @NotBlank(message = "Mô tả danh mục không được bỏ trống")
    private String description;
}
