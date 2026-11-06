package com.example.Quanlysanpham.dto.response.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryReportResponseDTO {
    private Integer categoryId;
    private String categoryName;
    private Long totalProducts;
}
