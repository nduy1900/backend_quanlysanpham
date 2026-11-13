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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private static final Logger logger = LoggerFactory.getLogger(BrandService.class);

    // Lấy danh sách
    @Cacheable("brands")
    public List<BrandResponseDTO> getAll() {
        logger.info("API được gọi: BrandController.getBrandList()");
        List<Brand> brands = brandRepository.findAll();
        logger.info("Số lượng thương hiệu trong danh sách là: " + brands.size());
        return brands
                .stream()
                .map(brandMapper::toResponse).toList();
    }

    // Lấy theo id
    @Cacheable(
            value = "brandDetail",
            key = "#id"
    )
    public BrandResponseDTO getBrandById(int id) {
        logger.info("API được gọi: BrandController.getBrandById()");
        logger.info("Lấy thương hiệu, ID: " + id);
        Brand findBrandInDB = brandRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy thương hiệu, ID: " + id);
                    return new ResourceNotFoundException("Thương hiệu không tồn tại");
                }
        );
        logger.info("Tìm thấy thương hiệu, ID: " + id);
        return brandMapper.toResponse(findBrandInDB);
    }

    // Tạo thương hiệu
    @CacheEvict(
            cacheNames = {
                    "brands",
                    "brandDetail"
            },
            allEntries = true
    )
    public BrandResponseDTO createBrand(BrandRequestDTO request, Authentication authentication) {
        logger.info("API được gọi: BrandController.postBrand()");
        logger.info("Bắt đầu tạo thương hiệu " + request.getName());
        Brand newBrand = new Brand();
        newBrand.setName(request.getName());
        newBrand.setCountry(request.getCountry());

        Brand savedBrand = brandRepository.save(newBrand);
        logger.info("Tạo thương hiệu " + request.getName() + " thành công");
        // Ghi nhận lịch sử tạo
        auditLogService.saveLog(authentication.getName(), "CREATE", "Brand", savedBrand.getId());
        return brandMapper.toResponse(savedBrand);
    }

    // Cập nhật thương hiệu
    @Caching(
            put = {
                    @CachePut(
                            value = "brandDetail",
                            key = "#id"
                    )
            },
            evict = {
                    @CacheEvict(
                            value = "brands", allEntries = true
                    )
            }
    )
    public BrandResponseDTO updateBrand(int id, BrandRequestDTO request, Authentication authentication) {
        logger.info("API được gọi: BrandController.putBrand()");
        logger.info("Cập nhật thương hiệu, ID: " + id);
        Brand updatedBrand = brandRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy thương hiệu, ID: " + id);
                    return new ResourceNotFoundException("Thương hiệu không tồn tại");
                }
        );

        updatedBrand.setName(request.getName());
        updatedBrand.setCountry(request.getCountry());

        Brand savedBrand = brandRepository.save(updatedBrand);
        logger.info("Cập nhật thương hiệu, ID: " + id);

        // Ghi nhận lịch sử cập nhật
        auditLogService.saveLog(authentication.getName(), "UPDATE", "Brand", savedBrand.getId());
        return brandMapper.toResponse(savedBrand);
    }

    // Xoá thương hiệu
    @Caching(
            evict = {
                    @CacheEvict(value = "brands", allEntries = true),
                    @CacheEvict(value = "brandDetail", key = "#id")
            }
    )
    public void deleteBrand(int id, Authentication authentication) {
        logger.info("API được gọi: BrandController.deleteBrand()");
        logger.info("Xoá thương hiệu, ID: " + id);
        Brand deletedBrand = brandRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tồn tại thương hiệu, ID: " + id);
                    return new ResourceNotFoundException("Thương hiệu không tồn tại");
                }
        );

        // Kiểm tra Brand có sản phẩm không
        if (productRepository.existsByBrand_Id(id)) {
            throw new DuplicateResourceException(
                    "Không thể xóa thương hiệu đang có sản phẩm"
            );
        }
        brandRepository.delete(deletedBrand);
        logger.info("Xoá thương hiệu, ID: " + id);
        // Ghi nhận lịch sử xoá
        auditLogService.saveLog(authentication.getName(), "DELETE", "Brand", deletedBrand.getId());
    }
}
