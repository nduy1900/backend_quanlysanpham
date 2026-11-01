package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.stock_movement.StockMovementResponseDTO;
import com.example.Quanlysanpham.entity.StockMovement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {
    StockMovementResponseDTO toResponse(StockMovement stockMovement);
}
