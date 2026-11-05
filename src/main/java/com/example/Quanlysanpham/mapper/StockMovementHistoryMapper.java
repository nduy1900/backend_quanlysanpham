package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.stock_movement.StockMovementHistoryResponseDTO;
import com.example.Quanlysanpham.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementHistoryMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    StockMovementHistoryResponseDTO toResponse(StockMovement stockMovement);
}
