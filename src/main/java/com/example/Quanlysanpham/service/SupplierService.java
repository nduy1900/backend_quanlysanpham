package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.supplier.SupplierRequestDTO;
import com.example.Quanlysanpham.dto.response.supplier.SupplierResponseDTO;
import com.example.Quanlysanpham.entity.Supplier;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.SupplierMapper;
import com.example.Quanlysanpham.repository.SupplierRepository;
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
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final AuditLogService auditLogService;
    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);

    // Lấy danh sách
    @Cacheable("suppliers")
    public List<SupplierResponseDTO> getAll() {
        logger.info("API được gọi: SupplierController.getList()");
        List<Supplier> suppliers = supplierRepository.findAll();
        logger.info("Số lượng nhà cung cấp trong danh sách là: " + suppliers.size());
        return suppliers
                .stream()
                .map(supplierMapper::toResponse).toList();
    }

    // Lấy theo id
    @Cacheable(
            value = "supplierDetail",
            key = "#id"
    )
    public SupplierResponseDTO getById(int id) {
        logger.info("API được gọi: SupplierController.getSupplierById()");
        logger.info("Tìm nhà cung cấp, ID: " + id);
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy nhà cung cấp, ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy nhà cung cấp");
                }
        );
        logger.info("Tìm nhà cung cấp, ID: " + id);
        return supplierMapper.toResponse(supplier);
    }

    // Tạo mới nhà cung cấp
    @CacheEvict(
            cacheNames = {
                    "suppliers",
                    "supplierDetail"
            },
            allEntries = true
    )
    public SupplierResponseDTO createSupplier(SupplierRequestDTO request, Authentication authentication) {
        logger.info("API được gọi: SupplierController.postSupplier()");
        logger.info("Tạo mới nhà cung cấp " + request.getName());
        Supplier newSupplier = new Supplier();
        newSupplier.setName(request.getName());
        newSupplier.setPhone(request.getPhone());
        newSupplier.setEmail(request.getEmail());
        newSupplier.setAddress(request.getAddress());

        Supplier savedSupplier = supplierRepository.save(newSupplier);
        logger.info("Tạo thành công nhà cung cấp " + request.getName());
        auditLogService.saveLog(authentication.getName(), "CREATE", "Supplier", savedSupplier.getId());
        return supplierMapper.toResponse(savedSupplier);
    }

    // Cập nhật nhà cung cấp
    @Caching(
            put = {
                    @CachePut(
                            value = "supplierDetail",
                            key = "#id"
                    )
            },
            evict = {
                    @CacheEvict(
                            value = "suppliers",
                            allEntries = true
                    )
            }
    )
    public SupplierResponseDTO updateSupplier(int id, SupplierRequestDTO request, Authentication authentication) {
        logger.info("API được gọi: SupplierController.putSupplier()");
        logger.info("Cập nhật nhà cung cấp, ID: " + id);
        Supplier updatedSupplier = supplierRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy nhà cung cấp, ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy nhà cung cấp");
                }
        );
        updatedSupplier.setName(request.getName());
        updatedSupplier.setPhone(request.getPhone());
        updatedSupplier.setEmail(request.getEmail());
        updatedSupplier.setAddress(request.getAddress());

        Supplier savedSupplier = supplierRepository.save(updatedSupplier);
        logger.info("Cập nhật nhà cung cấp, ID: " + id);
        auditLogService.saveLog(authentication.getName(), "UPDATE", "Supplier", savedSupplier.getId());
        return supplierMapper.toResponse(savedSupplier);
    }

    // Xoá nhà cung cấp
    @Caching(
            evict = {
                    @CacheEvict(value = "suppliers", allEntries = true),
                    @CacheEvict(value = "supplierDetail", key = "#id")
            }
    )
    public void deleteSupplier(int id, Authentication authentication) {
        logger.info("API được gọi: SupplierController.deleteSupplier()");
        Supplier deletedSupplier = supplierRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy nhà cung cấp, ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy nhà cung cấp");
                }
        );
        supplierRepository.delete(deletedSupplier);
        logger.info("Xoá nhà cung cấp, ID: " + id);
        auditLogService.saveLog(authentication.getName(), "DELETE", "Supplier", deletedSupplier.getId());
    }
}
