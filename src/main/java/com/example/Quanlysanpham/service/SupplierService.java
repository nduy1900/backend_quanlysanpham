package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.supplier.SupplierRequestDTO;
import com.example.Quanlysanpham.dto.response.supplier.SupplierResponseDTO;
import com.example.Quanlysanpham.entity.Supplier;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.SupplierMapper;
import com.example.Quanlysanpham.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    // Lấy danh sách
    public List<SupplierResponseDTO> getAll() {
        List<Supplier> suppliers = supplierRepository.findAll();
        return suppliers
                .stream()
                .map(supplierMapper::toResponse).toList();
    }

    // Lấy theo id
    public SupplierResponseDTO getById(int id) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp")
        );
        return supplierMapper.toResponse(supplier);
    }

    // Tạo mới nhà cung cấp
    public SupplierResponseDTO createSupplier(SupplierRequestDTO request) {
        Supplier newSupplier = new Supplier();
        newSupplier.setName(request.getName());
        newSupplier.setPhone(request.getPhone());
        newSupplier.setEmail(request.getEmail());
        newSupplier.setAddress(request.getAddress());

        Supplier savedSupplier = supplierRepository.save(newSupplier);
        return supplierMapper.toResponse(savedSupplier);
    }

    // Cập nhật nhà cung cấp
    public SupplierResponseDTO updateSupplier(int id, SupplierRequestDTO request) {
        Supplier updatedSupplier = supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp")
        );
        updatedSupplier.setName(request.getName());
        updatedSupplier.setPhone(request.getPhone());
        updatedSupplier.setEmail(request.getEmail());
        updatedSupplier.setAddress(request.getAddress());

        Supplier savedSupplier = supplierRepository.save(updatedSupplier);
        return supplierMapper.toResponse(savedSupplier);
    }

    // Xoá nhà cung cấp
    public void deleteSupplier(int id) {
        Supplier deletedSupplier = supplierRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp")
        );
        supplierRepository.delete(deletedSupplier);
    }
}
