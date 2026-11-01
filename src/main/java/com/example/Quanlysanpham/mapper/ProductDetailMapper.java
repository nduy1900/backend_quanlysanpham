package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.product_detail.ProductDetailResponseDTO;
import com.example.Quanlysanpham.entity.ProductDetail;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductDetailMapper {
    ProductDetailResponseDTO toResponse(ProductDetail productDetail);
}
