package com.example.Quanlysanpham.service;

import com.cloudinary.Cloudinary;
import com.example.Quanlysanpham.dto.request.product.ProductRequestDTO;
import com.example.Quanlysanpham.dto.request.product_detail.ProductDetailRequestDTO;
import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.entity.Brand;
import com.example.Quanlysanpham.entity.Category;
import com.example.Quanlysanpham.entity.Product;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.ProductMapper;
import com.example.Quanlysanpham.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setSku("SP001");
        product.setName("Laptop Dell");
        product.setPrice(15000000);
        product.setQuantity(10);
        product.setMinStock(2);
        product.setStatus("ACTIVE");
        product.setDeleted(false);

        responseDTO = new ProductResponseDTO();

    }


    // getProductById()
    @Nested
    @DisplayName("getProductById")
    class GetProductById {
        @Test
        @DisplayName("Kiểm tra lấy sản phẩm theo ID thành công")
        void getProductById_WhenProductExists_ShouldReturnProduct() {
            // Arrange
            when(productRepository.findById(1))
                    .thenReturn(Optional.of(product));

            when(productMapper.toResponse(product))
                    .thenReturn(responseDTO);

            // Actual
            ProductResponseDTO result = productService.getProductById(1);

            // Assertion
            assertNotNull(result);
            assertSame(responseDTO, result);

            verify(productRepository).findById(1);
            verify(productMapper).toResponse(product);
        }


        @Test
        @DisplayName("Kiểm tra lấy sản phẩm theo ID không tồn tại")
        void getProductById_WhenProductNotExists_ShouldThrowException() {
            when(productRepository.findById(99))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> productService.getProductById(99)
            );


            verify(productRepository).findById(99);
        }
    }

    // createProduct
    @Nested
    @DisplayName("createProduct")
    class CreateProduct {
        @Test
        @DisplayName("Kiểm tra tạo sản phẩm thành công")
        void createProduct_ShouldReturnCreatedProduct() {
            // Arrange
            ProductRequestDTO request = new ProductRequestDTO();

            request.setSku("SP002");
            request.setName("Laptop HP");
            request.setPrice(18000000);
            request.setQuantity(20);
            request.setMinStock(5);
            request.setCategoryId(1);
            request.setBrandId(1);

            ProductDetailRequestDTO detail = new ProductDetailRequestDTO();

            request.setProductDetail(detail);
            request.setProductSuppliers(new ArrayList<>());

            User user = new User();
            user.setId(10);

            Category category = new Category();
            Brand brand = new Brand();

            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(user));

            when(productRepository.existsBySku("SP002"))
                    .thenReturn(false);

            when(categoryRepository.findById(1))
                    .thenReturn(Optional.of(category));

            when(brandRepository.findById(1))
                    .thenReturn(Optional.of(brand));

            when(productRepository.save(any(Product.class)))
                    .thenAnswer(invocation -> {
                        Product saved = invocation.getArgument(0);
                        saved.setId(2);
                        return saved;
                    });

            when(productMapper.toResponse(any(Product.class)))
                    .thenReturn(responseDTO);

            when(authentication.getName()).thenReturn("admin");

            // Act
            ProductResponseDTO result =
                    productService.createProduct(request, authentication);

            // Assert
            assertNotNull(result);

            verify(userRepository).findByUsername("admin");
            verify(productRepository).existsBySku("SP002");
            verify(categoryRepository).findById(1);
            verify(brandRepository).findById(1);
            verify(productRepository).save(any(Product.class));

            verify(auditLogService).saveLog(
                    "admin", "CREATE", "Product", 2
            );
        }
    }


}
