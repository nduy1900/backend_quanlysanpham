package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.product_image.ProductImageResponseDTO;
import com.example.Quanlysanpham.entity.ProductImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {
    ProductImageResponseDTO toResponse(ProductImage productImage);
}
