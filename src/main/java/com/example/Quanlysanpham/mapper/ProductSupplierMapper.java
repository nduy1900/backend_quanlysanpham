package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.product_supplier.ProductSupplierResponseDTO;
import com.example.Quanlysanpham.entity.ProductSupplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = SupplierMapper.class)
public interface ProductSupplierMapper {
    ProductSupplierResponseDTO toResponse(ProductSupplier productSupplier);
}
