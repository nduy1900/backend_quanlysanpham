package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.supplier.SupplierResponseDTO;
import com.example.Quanlysanpham.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    SupplierResponseDTO toResponse(Supplier supplier);
}
