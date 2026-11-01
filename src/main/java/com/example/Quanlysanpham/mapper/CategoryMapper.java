package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.category.CategoryResponseDTO;
import com.example.Quanlysanpham.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDTO toResponse(Category category);
}
