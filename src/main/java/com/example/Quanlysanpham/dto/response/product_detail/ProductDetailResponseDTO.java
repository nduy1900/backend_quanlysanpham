package com.example.Quanlysanpham.dto.response.product_detail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponseDTO {
    private int id;
    private String description;
    private double weight;
    private String dimensions;
    private String color;
    private int warrantyMonths;
}
