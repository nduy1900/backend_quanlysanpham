package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.brand.BrandResponseDTO;
import com.example.Quanlysanpham.entity.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponseDTO toResponse(Brand brand);
}
