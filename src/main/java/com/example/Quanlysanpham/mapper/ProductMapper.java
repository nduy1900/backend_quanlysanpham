package com.example.Quanlysanpham.mapper;

import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
        uses = {
                CategoryMapper.class,
                BrandMapper.class,
                ProductDetailMapper.class,
                ProductImageMapper.class,
                ProductSupplierMapper.class,
                StockMovementMapper.class

        }
)
public interface ProductMapper {
    ProductResponseDTO toResponse(Product product);
}
