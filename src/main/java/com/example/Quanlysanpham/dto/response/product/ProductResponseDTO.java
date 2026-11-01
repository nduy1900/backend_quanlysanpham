package com.example.Quanlysanpham.dto.response.product;

import com.example.Quanlysanpham.dto.response.brand.BrandResponseDTO;
import com.example.Quanlysanpham.dto.response.category.CategoryResponseDTO;
import com.example.Quanlysanpham.dto.response.product_detail.ProductDetailResponseDTO;
import com.example.Quanlysanpham.dto.response.product_image.ProductImageResponseDTO;
import com.example.Quanlysanpham.dto.response.product_supplier.ProductSupplierResponseDTO;
import com.example.Quanlysanpham.dto.response.stock_movement.StockMovementResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private int id;
    private String sku;
    private String name;
    private double price;
    private int quantity;
    private int minStock;
    private String status;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int createdBy;
    private int updatedBy;

    // Quan hệ
    private CategoryResponseDTO category;
    private BrandResponseDTO brand;
    private ProductDetailResponseDTO productDetail;
    private List<ProductImageResponseDTO> productImages;
    private List<ProductSupplierResponseDTO> productSuppliers;
    private List<StockMovementResponseDTO> stockMovements;
}
