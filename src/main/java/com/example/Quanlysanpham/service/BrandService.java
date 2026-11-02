package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.brand.BrandRequestDTO;
import com.example.Quanlysanpham.dto.response.brand.BrandResponseDTO;
import com.example.Quanlysanpham.entity.Brand;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.BrandMapper;
import com.example.Quanlysanpham.repository.BrandRepository;
import com.example.Quanlysanpham.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final ProductRepository productRepository;

    // Lấy danh sách
    public List<BrandResponseDTO> getAll() {
        List<Brand> brands = brandRepository.findAll();
        return brands
                .stream()
                .map(brandMapper::toResponse).toList();
    }

    // Lấy theo id
    public BrandResponseDTO getBrandById(int id) {
        Brand findBrandInDB = brandRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Thương hiệu không tồn tại")
        );
        return brandMapper.toResponse(findBrandInDB);
    }

    // Tạo thương hiệu
    public BrandResponseDTO createBrand(BrandRequestDTO request) {
        Brand newBrand = new Brand();
        newBrand.setName(request.getName());
        newBrand.setCountry(request.getCountry());

        Brand savedBrand = brandRepository.save(newBrand);
        return brandMapper.toResponse(savedBrand);
    }

    // Cập nhật thương hiệu
    public BrandResponseDTO updateBrand(int id, BrandRequestDTO request) {
        Brand updatedBrand = brandRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Thương hiệu không tồn tại")
        );

        updatedBrand.setName(request.getName());
        updatedBrand.setCountry(request.getCountry());

        Brand savedBrand = brandRepository.save(updatedBrand);
        return brandMapper.toResponse(savedBrand);
    }

    // Xoá thương hiệu
    public void deleteBrand(int id) {
        Brand deletedBrand = brandRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Thương hiệu không tồn tại")
        );

        // Kiểm tra Brand có sản phẩm không
        if (productRepository.existsByBrand_Id(id)) {
            throw new DuplicateResourceException(
                    "Không thể xóa thương hiệu đang có sản phẩm"
            );
        }
        brandRepository.delete(deletedBrand);
    }
}
