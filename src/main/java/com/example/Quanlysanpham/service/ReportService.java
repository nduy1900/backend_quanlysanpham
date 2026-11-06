package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.response.category.CategoryReportResponseDTO;
import com.example.Quanlysanpham.mapper.ProductMapper;
import com.example.Quanlysanpham.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    // Lấy số sản phẩm theo category
    public List<CategoryReportResponseDTO> getProductByCategory() {
        List<Object[]> results = productRepository.findProductByCategory();
        List<CategoryReportResponseDTO> dtoList = new ArrayList<>();

        for (Object[] row : results) {
            CategoryReportResponseDTO dto = new CategoryReportResponseDTO();
            dto.setCategoryId((Integer) row[0]);
            dto.setCategoryName((String) row[1]);
            dto.setTotalProducts((Long) row[2]);

            dtoList.add(dto);
        }
        return dtoList;
    }

    // Tổng giá trị tồn kho
    public BigDecimal getInventoryValue() {
        return productRepository.getInventoryValue();
    }
}
