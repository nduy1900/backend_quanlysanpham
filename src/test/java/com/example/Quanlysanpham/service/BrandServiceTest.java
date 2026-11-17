package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.brand.BrandRequestDTO;
import com.example.Quanlysanpham.dto.response.brand.BrandResponseDTO;
import com.example.Quanlysanpham.entity.Brand;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.BrandMapper;
import com.example.Quanlysanpham.repository.BrandRepository;
import com.example.Quanlysanpham.repository.ProductRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {
    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandMapper brandMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BrandService brandService;

    private Brand brand;
    private BrandResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        brand = new Brand();
        brand.setId(1);
        brand.setName("Dell");
        brand.setCountry("USA");

        responseDTO = new BrandResponseDTO();
        responseDTO.setId(1);
        responseDTO.setName("Dell");
        responseDTO.setCountry("USA");
    }

    // getAll: Lấy danh sách thương hiệu
    @Nested
    @DisplayName("getAll")
    class GetAllTest {
        @Test
        @DisplayName("Kiểm tra lấy danh sách thương hiệu thành công")
        void getAll_ShouldReturnListBrandSuccessfully() {
            when(brandRepository.findAll()).thenReturn(List.of(brand));
            when(brandMapper.toResponse(brand)).thenReturn(responseDTO);

            List<BrandResponseDTO> result = brandService.getAll();

            assertNotNull(result);
            assertEquals("Dell", result.get(0).getName());
            assertEquals("USA", result.get(0).getCountry());

            verify(brandRepository).findAll();
            verify(brandMapper).toResponse(brand);
        }
    }

    // getBrandById: Lấy thương hiệu theo id
    @Nested
    @DisplayName("getBrandById")
    class GetBrandByIdTest {
        @Test
        @DisplayName("Kiểm tra lấy thương hiệu theo ID thành công")
        void getBrandById_ShouldReturnBrand() {
            when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
            when(brandMapper.toResponse(brand)).thenReturn(responseDTO);

            BrandResponseDTO result = brandService.getBrandById(1);

            assertNotNull(result);
            assertEquals(1, result.getId());
            assertSame(responseDTO, result);

            verify(brandRepository).findById(1);
            verify(brandMapper).toResponse(brand);
        }

        @Test
        @DisplayName("Kiểm tra lấy thương hiệu không tồn tại")
        void getBrandById_WhenBrandNotExists_ShouldThrowException() {
            when(brandRepository.findById(88)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> brandService.getBrandById(88));

            verify(brandRepository).findById(88);
            verifyNoInteractions(brandMapper);
        }
    }

    // createBrand: Tạo thương hiệu
    @Nested
    @DisplayName("createBrand")
    class CreateBrandTest {
        @Test
        @DisplayName("Kiểm tra tạo thương hiệu thành công")
        void createBrand_ShouldReturnNewBrand() {
            BrandRequestDTO request = new BrandRequestDTO();
            request.setName("Dell");
            request.setCountry("USA");

            Brand newBrand = new Brand();
            newBrand.setId(1);
            newBrand.setName(request.getName());
            newBrand.setCountry(request.getCountry());

            when(authentication.getName()).thenReturn("admin");
            when(brandRepository.save(any(Brand.class))).thenReturn(newBrand);
            when(brandMapper.toResponse(newBrand)).thenReturn(responseDTO);

            BrandResponseDTO result = brandService.createBrand(request, authentication);

            assertNotNull(result);
            assertEquals("Dell", result.getName());
            assertEquals("USA", result.getCountry());
            assertSame(responseDTO, result);

            verify(brandRepository).save(any(Brand.class));
            verify(brandMapper).toResponse(newBrand);
            verify(auditLogService).saveLog("admin", "CREATE", "Brand", 1);
        }

        @Test
        @DisplayName("Kiểm tra thông tin thương hiệu được truyền vào repository")
        void createBrand_ShouldSaveCorrectBrandInformation() {
            BrandRequestDTO request = new BrandRequestDTO();
            request.setName("Dell");
            request.setCountry("USA");

            when(authentication.getName()).thenReturn("admin");
            when(brandRepository.save(any(Brand.class))).thenReturn(brand);
            when(brandMapper.toResponse(any(Brand.class))).thenReturn(responseDTO);

            ArgumentCaptor<Brand> brandCaptor = ArgumentCaptor.forClass(Brand.class);


            brandService.createBrand(request, authentication);


            verify(brandRepository).save(brandCaptor.capture());
            Brand savedBrand = brandCaptor.getValue();

            assertEquals("Dell", savedBrand.getName());
            assertEquals("USA", savedBrand.getCountry());
        }

        @Test
        @DisplayName("Tạo thương hiệu thất bại khi lưu database xảy ra lỗi")
        void createBrand_WhenSaveFails_ShouldThrowException() {
            BrandRequestDTO request = new BrandRequestDTO();
            request.setName("Samsung");
            request.setCountry("South Korea");

            when(brandRepository.save(any(Brand.class)))
                    .thenThrow(new RuntimeException("Database error"));


            assertThrows(
                    RuntimeException.class,
                    () -> brandService.createBrand(request, authentication)
            );


            verify(brandRepository).save(any(Brand.class));
            verifyNoInteractions(auditLogService);
            verifyNoInteractions(brandMapper);
        }
    }

    // updateBrand: Cập nhật thương hiệu
    @Nested
    @DisplayName("updateBrand")
    class UpdateBrandTest {
        @Test
        @DisplayName("Kiểm tra cập nhật thương hiệu thành công")
        void updateBrand_ShouldReturnUpdatedBrand() {
            BrandRequestDTO request = new BrandRequestDTO();
            request.setName("Dell");
            request.setCountry("USA");

            when(authentication.getName()).thenReturn("admin");
            when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
            when(brandRepository.save(any(Brand.class))).thenReturn(brand);
            when(brandMapper.toResponse(brand)).thenReturn(responseDTO);
            ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);

            BrandResponseDTO result = brandService.updateBrand(1, request, authentication);

            verify(brandRepository).save(captor.capture());

            assertNotNull(result);
            assertEquals("Dell", captor.getValue().getName());
            assertEquals("USA", captor.getValue().getCountry());

            verify(brandRepository).findById(1);
            verify(brandMapper).toResponse(brand);
            verify(auditLogService).saveLog("admin", "UPDATE", "Brand", 1);
        }

        @Test
        @DisplayName("Kiểm tra cập nhật thương hiệu không tồn tại")
        void updateBrand_WhenBrandNotExists_ShouldThrowException() {
            BrandRequestDTO request = new BrandRequestDTO();
            request.setName("Dell");
            request.setCountry("USA");

            when(brandRepository.findById(80)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class
                    , () -> brandService.updateBrand(80, request, authentication));

            verify(brandRepository, never()).save(any(Brand.class));
            verifyNoInteractions(brandMapper);
            verifyNoInteractions(auditLogService);
        }
    }

    // deleteBrand: Xoá thương hiệu
    @Nested
    @DisplayName("deleteBrand")
    class DeleteBrand {
        @Test
        @DisplayName("Kiểm tra xoá thương hiệu thành công")
        void deleteBrand_ShouldReturnDeleteSuccessfully() {
            when(authentication.getName()).thenReturn("admin");
            when(brandRepository.findById(1)).thenReturn(Optional.of(brand));


            brandService.deleteBrand(1, authentication);

            verify(brandRepository).findById(1);
            verify(brandRepository).delete(brand);
            verify(auditLogService).saveLog("admin", "DELETE", "Brand", 1);

        }

        @Test
        @DisplayName("Kiểm tra xoá thương hiệu không tồn tại")
        void deleteBrand_WhenBrandNotExists_ShouldThrowException() {
            when(brandRepository.findById(80)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> brandService.deleteBrand(80, authentication)
            );

            assertEquals("Thương hiệu không tồn tại", exception.getMessage());

            verify(brandRepository).findById(80);
            verify(brandRepository, never()).save(any(Brand.class));
            verifyNoInteractions(auditLogService);
        }
    }

}
