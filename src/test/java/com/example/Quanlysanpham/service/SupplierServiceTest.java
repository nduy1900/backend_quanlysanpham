package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.supplier.SupplierRequestDTO;
import com.example.Quanlysanpham.dto.response.supplier.SupplierResponseDTO;
import com.example.Quanlysanpham.entity.Supplier;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.SupplierMapper;
import com.example.Quanlysanpham.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {
    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private SupplierResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1);
        supplier.setName("Công ty Logitech Việt Nam");
        supplier.setPhone("0906789012");
        supplier.setEmail("logitech@example.com");
        supplier.setAddress("Hà Nội");

        responseDTO = new SupplierResponseDTO();
        responseDTO.setId(1);
        responseDTO.setName("Công ty Logitech Việt Nam");
        responseDTO.setPhone("0906789012");
        responseDTO.setEmail("logitech@example.com");
        responseDTO.setAddress("Hà Nội");
    }

    // getAll: Lấy danh sách nhà cung cấp
    @Nested
    @DisplayName("getAll")
    class GetAllTest {
        @Test
        @DisplayName("Kiểm tra lấy danh sách nhà cung cấp thành công")
        void getAll_ShouldReturnListSupplierSuccessfully() {
            when(supplierRepository.findAll()).thenReturn(List.of(supplier));
            when(supplierMapper.toResponse(supplier)).thenReturn(responseDTO);

            List<SupplierResponseDTO> result = supplierService.getAll();

            assertNotNull(result);
            assertEquals("Công ty Logitech Việt Nam", result.get(0).getName());
            assertEquals("0906789012", result.get(0).getPhone());
            assertEquals("logitech@example.com", result.get(0).getEmail());
            assertEquals("Hà Nội", result.get(0).getAddress());

            verify(supplierRepository).findAll();
            verify(supplierMapper).toResponse(supplier);
        }

        @Test
        @DisplayName("Kiểm tra lấy danh sách nhà cung cấp rỗng")
        void getAll_WhenSupplierListIsEmpty_ShouldReturnEmptyList() {
            when(supplierRepository.findAll()).thenReturn(Collections.emptyList());

            List<SupplierResponseDTO> result = supplierService.getAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(supplierRepository).findAll();
            verifyNoInteractions(supplierMapper);
        }
    }

    // getSupplierById: Lấy nhà cung cấp theo id
    @Nested
    @DisplayName("getSupplierById")
    class GetSupplierByIdTest {
        @Test
        @DisplayName("Kiểm tra lấy nhà cung cấp theo ID thành công")
        void getById_ShouldReturnSupplierSuccessfully() {
            when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

            when(supplierMapper.toResponse(supplier)).thenReturn(responseDTO);

            SupplierResponseDTO result = supplierService.getById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Công ty Logitech Việt Nam", result.getName());
            assertEquals("0906789012", result.getPhone());
            assertEquals("logitech@example.com", result.getEmail());

            verify(supplierRepository).findById(1);
            verify(supplierMapper).toResponse(supplier);
        }

        @Test
        @DisplayName("Kiểm tra nhà cung cấp không tồn tại ID")
        void getById_WhenSupplierNotFound_ShouldThrowException() {
            when(supplierRepository.findById(99))
                    .thenReturn(Optional.empty());


            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> supplierService.getById(99)
            );

            assertEquals("Không tìm thấy nhà cung cấp", exception.getMessage());

            verify(supplierRepository).findById(99);
            verifyNoInteractions(supplierMapper);
        }
    }

    // createSupplier: Tạo mới nhà cung cấp
    @Nested
    @DisplayName("createSupplier")
    class CreateSupplier {
        @Test
        @DisplayName("Kiểm tra tạo nhà cung cấp thành công")
        void createSupplier_ShouldReturnCreatedSupplierSuccessfully() {
            SupplierRequestDTO request = new SupplierRequestDTO();
            request.setName("Công ty Logitech Việt Nam");
            request.setPhone("0906789012");
            request.setEmail("logitech@example.com");
            request.setAddress("Hà Nội");

            when(authentication.getName()).thenReturn("admin");

            when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

            when(supplierMapper.toResponse(supplier)).thenReturn(responseDTO);


            SupplierResponseDTO result =
                    supplierService.createSupplier(request, authentication);


            assertNotNull(result);
            assertEquals(1, result.getId());
            assertEquals("Công ty Logitech Việt Nam", result.getName());


            verify(supplierRepository).save(any(Supplier.class));
            verify(auditLogService).saveLog(
                    "admin",
                    "CREATE",
                    "Supplier",
                    1
            );

            verify(supplierMapper).toResponse(supplier);
        }

        @Test
        @DisplayName("Kiểm tra dữ liệu nhà cung cấp trước khi lưu")
        void createSupplier_ShouldSaveCorrectSupplierInformation() {

            SupplierRequestDTO request = new SupplierRequestDTO();
            request.setName("Công ty Logitech Việt Nam");
            request.setPhone("0906789012");
            request.setEmail("logitech@example.com");
            request.setAddress("Hà Nội");

            when(authentication.getName()).thenReturn("admin");

            when(supplierRepository.save(any(Supplier.class)))
                    .thenReturn(supplier);

            when(supplierMapper.toResponse(supplier))
                    .thenReturn(responseDTO);

            ArgumentCaptor<Supplier> captor =
                    ArgumentCaptor.forClass(Supplier.class);


            supplierService.createSupplier(request, authentication);


            verify(supplierRepository).save(captor.capture());

            Supplier capturedSupplier = captor.getValue();

            assertEquals("Công ty Logitech Việt Nam", capturedSupplier.getName());
            assertEquals("0906789012", capturedSupplier.getPhone());
            assertEquals("logitech@example.com", capturedSupplier.getEmail());
            assertEquals("Hà Nội", capturedSupplier.getAddress());
        }
    }

    // updateSupplier: Cập nhật nhà cung cấp
    @Nested
    @DisplayName("updateSupplier")
    class UpdateSupplierTest {
        @Test
        @DisplayName("Kiểm tra cập nhật nhà cung cấp thành công")
        void updateSupplier_ShouldReturnUpdatedSupplierSuccessfully() {
            SupplierRequestDTO request = new SupplierRequestDTO();
            request.setName("Công ty Logitech Việt Nam");
            request.setPhone("0906789012");
            request.setEmail("logitech@example.com");
            request.setAddress("Hà Nội");

            when(authentication.getName()).thenReturn("admin");

            when(supplierRepository.findById(1))
                    .thenReturn(Optional.of(supplier));

            when(supplierRepository.save(any(Supplier.class)))
                    .thenReturn(supplier);

            when(supplierMapper.toResponse(supplier))
                    .thenReturn(responseDTO);

            ArgumentCaptor<Supplier> captor =
                    ArgumentCaptor.forClass(Supplier.class);


            SupplierResponseDTO result =
                    supplierService.updateSupplier(1, request, authentication);


            assertNotNull(result);

            verify(supplierRepository).save(captor.capture());

            Supplier capturedSupplier = captor.getValue();

            assertEquals("Công ty Logitech Việt Nam", capturedSupplier.getName());
            assertEquals("0906789012", capturedSupplier.getPhone());
            assertEquals("logitech@example.com", capturedSupplier.getEmail());
            assertEquals("Hà Nội", capturedSupplier.getAddress());


            verify(supplierRepository).findById(1);
            verify(supplierMapper).toResponse(supplier);

            verify(auditLogService).saveLog(
                    "admin",
                    "UPDATE",
                    "Supplier",
                    1
            );
        }

        @Test
        @DisplayName(" Kiểm tra cập nhật thất bại khi không tìm thấy nhà cung cấp")
        void updateSupplier_WhenSupplierNotFound_ShouldThrowException() {
            SupplierRequestDTO request = new SupplierRequestDTO();
            request.setName("Công ty Logitech Việt Nam");
            request.setPhone("0906789012");
            request.setEmail("logitech@example.com");
            request.setAddress("Hà Nội");

            when(supplierRepository.findById(99))
                    .thenReturn(Optional.empty());


            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> supplierService.updateSupplier(99, request, authentication)
            );

            assertEquals("Không tìm thấy nhà cung cấp", exception.getMessage());


            verify(supplierRepository).findById(99);
            verify(supplierRepository, never()).save(any(Supplier.class));

            verifyNoInteractions(auditLogService);
            verifyNoInteractions(supplierMapper);
        }
    }

    // deleteSupplier: Xoá nhà cung cấp
    @Nested
    @DisplayName("deleteSupplier")
    class DeleteSupplierTest {
        @Test
        @DisplayName("Kiểm tra xóa nhà cung cấp thành công")
        void deleteSupplier_ShouldDeleteSupplierSuccessfully() {
            when(authentication.getName()).thenReturn("admin");

            when(supplierRepository.findById(1))
                    .thenReturn(Optional.of(supplier));

            supplierService.deleteSupplier(1, authentication);

            verify(supplierRepository).findById(1);
            verify(supplierRepository).delete(supplier);

            verify(auditLogService).saveLog(
                    "admin",
                    "DELETE",
                    "Supplier",
                    1
            );
        }

        @Test
        @DisplayName("Kiểm tra xóa thất bại khi không tìm thấy nhà cung cấp")
        void deleteSupplier_WhenSupplierNotFound_ShouldThrowException() {
            when(supplierRepository.findById(99))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> supplierService.deleteSupplier(99, authentication)
            );

            assertEquals("Không tìm thấy nhà cung cấp", exception.getMessage());
            
            verify(supplierRepository).findById(99);
            verify(supplierRepository, never()).delete(any(Supplier.class));

            verifyNoInteractions(auditLogService);
        }
    }
}
