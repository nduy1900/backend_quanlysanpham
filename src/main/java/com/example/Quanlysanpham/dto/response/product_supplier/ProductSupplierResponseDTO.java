package com.example.Quanlysanpham.dto.response.product_supplier;

import com.example.Quanlysanpham.dto.response.supplier.SupplierResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductSupplierResponseDTO {
    private int id;
    private double supplyPrice;
    private LocalDateTime createdAt;
    private SupplierResponseDTO supplier;
}
