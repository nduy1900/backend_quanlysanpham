package com.example.Quanlysanpham.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductExcelResponseDTO {
    private int id;
    private String sku;
    private String name;
    private double price;
    private int quantity;
    private int minStock;
    private String status;
    private boolean deleted;

}
