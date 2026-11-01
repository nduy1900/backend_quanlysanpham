package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.entity.Product;
import com.example.Quanlysanpham.mapper.ProductMapper;
import com.example.Quanlysanpham.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public List<ProductResponseDTO> getAll() {
        List<Product> products = productRepository.findAll();
        return products
                .stream()
                .map(productMapper::toResponse).toList();
    }
}
