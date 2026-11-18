package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.stock.StockMovementRequestDTO;
import com.example.Quanlysanpham.dto.response.stock_movement.StockMovementHistoryResponseDTO;
import com.example.Quanlysanpham.dto.response.stock_movement.StockMovementResponseDTO;
import com.example.Quanlysanpham.entity.Product;
import com.example.Quanlysanpham.entity.StockMovement;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.StockMovementHistoryMapper;
import com.example.Quanlysanpham.mapper.StockMovementMapper;
import com.example.Quanlysanpham.repository.ProductRepository;
import com.example.Quanlysanpham.repository.StockMovementRepository;
import com.example.Quanlysanpham.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockMovementTest {
    @Mock
    private StockMovementRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockMovementMapper stockMovementMapper;

    @Mock
    private StockMovementHistoryMapper stockMovementHistoryMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StockMovementService stockMovementService;

    private Product product;
    private User user;
    private StockMovement stockMovement;
    private StockMovementResponseDTO responseDTO;
    private StockMovementHistoryResponseDTO historyResponseDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setName("Samsung Galaxy S24");
        product.setQuantity(100);
        product.setDeleted(false);

        user = new User();
        user.setId(5);
        user.setUsername("admin");

        stockMovement = new StockMovement();
        stockMovement.setId(1);
        stockMovement.setType("IN");
        stockMovement.setQuantity(20);
        stockMovement.setNote("Nhập hàng");
        stockMovement.setProduct(product);
        stockMovement.setCreatedBy(5);

        responseDTO = new StockMovementResponseDTO();

        historyResponseDTO = new StockMovementHistoryResponseDTO();
    }

    // importProduct: Nhập kho
    @Nested
    @DisplayName("importProduct")
    class ImportProductTest {
        @Test
        @DisplayName("Kiểm tra nhập kho sản phẩm thành công")
        void importProduct_ShouldReturnStockMovementSuccessfully() {
            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(1);
            request.setQuantity(20);
            request.setNote("Nhập hàng");

            when(authentication.getName()).thenReturn("admin");

            when(productRepository.findById(1))
                    .thenReturn(Optional.of(product));

            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(user));

            when(productRepository.save(any(Product.class)))
                    .thenReturn(product);

            when(stockRepository.save(any(StockMovement.class)))
                    .thenReturn(stockMovement);

            when(stockMovementMapper.toResponse(stockMovement))
                    .thenReturn(responseDTO);

            // Act
            StockMovementResponseDTO result = stockMovementService.importProduct(request, authentication);

            // Assert
            assertNotNull(result);
            assertEquals(120, product.getQuantity());
            assertEquals(5, product.getUpdatedBy());

            // Verify
            verify(productRepository).findById(1);
            verify(userRepository).findByUsername("admin");
            verify(productRepository).save(product);
            verify(stockRepository).save(any(StockMovement.class));

            verify(auditLogService).saveLog(
                    "admin",
                    "IMPORT",
                    "Product",
                    1
            );

            verify(stockMovementMapper).toResponse(stockMovement);
        }


        @Test
        @DisplayName("Kiểm tra nhập kho thất bại khi số lượng nhỏ hơn hoặc bằng 0")
        void importProduct_WhenQuantityInvalid_ShouldThrowException() {
            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(1);
            request.setQuantity(0);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> stockMovementService.importProduct(request, authentication)
            );

            assertEquals("Số lượng nhập phải lớn hơn 0", exception.getMessage());

            // Verify
            verifyNoInteractions(
                    productRepository,
                    userRepository,
                    stockRepository,
                    auditLogService,
                    stockMovementMapper
            );
        }

        @Test
        @DisplayName("Kiểm tra nhập kho thất bại khi không tìm thấy sản phẩm")
        void importProduct_WhenProductNotFound_ShouldThrowException() {
            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(99);
            request.setQuantity(20);

            when(productRepository.findById(99))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockMovementService.importProduct(request, authentication)
            );

            assertEquals("Không tìm thấy sản phẩm nhập hàng", exception.getMessage());

            // Verify
            verify(productRepository).findById(99);

            verifyNoInteractions(
                    userRepository,
                    stockRepository,
                    auditLogService,
                    stockMovementMapper
            );
        }

        @Test
        @DisplayName("Kiểm tra nhập kho thất bại khi sản phẩm đã bị xóa")
        void importProduct_WhenProductDeleted_ShouldThrowException() {
            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(1);
            request.setQuantity(20);

            product.setDeleted(true);

            when(productRepository.findById(1))
                    .thenReturn(Optional.of(product));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> stockMovementService.importProduct(request, authentication)
            );

            assertEquals(
                    "Không thể nhập kho cho sản phẩm đã bị xóa",
                    exception.getMessage()
            );

            // Verify
            verify(productRepository).findById(1);
            verifyNoInteractions(
                    userRepository,
                    stockRepository,
                    auditLogService,
                    stockMovementMapper
            );
        }
    }

    // exportProduct: Xuất kho
    @Nested
    @DisplayName("exportProduct")
    class ExportProduct {
        @Test
        @DisplayName("Kiểm tra xuất kho sản phẩm thành công")
        void exportProduct_ShouldReturnStockMovementSuccessfully() {

            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(1);
            request.setQuantity(20);
            request.setNote("Xuất hàng");

            when(authentication.getName()).thenReturn("admin");

            when(productRepository.findById(1))
                    .thenReturn(Optional.of(product));

            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(user));

            when(productRepository.save(any(Product.class)))
                    .thenReturn(product);

            when(stockRepository.save(any(StockMovement.class)))
                    .thenReturn(stockMovement);

            when(stockMovementMapper.toResponse(stockMovement))
                    .thenReturn(responseDTO);

            // Act
            StockMovementResponseDTO result = stockMovementService.exportProduct(request, authentication);

            // Assert
            assertNotNull(result);
            assertEquals(80, product.getQuantity());
            assertEquals(5, product.getUpdatedBy());

            // Verify
            verify(productRepository).findById(1);
            verify(userRepository).findByUsername("admin");
            verify(productRepository).save(product);
            verify(stockRepository).save(any(StockMovement.class));

            verify(auditLogService).saveLog(
                    "admin",
                    "EXPORT",
                    "Product",
                    1
            );

            verify(stockMovementMapper).toResponse(stockMovement);
        }

        @Test
        @DisplayName("Kiểm tra xuất kho thất bại khi số lượng nhỏ hơn hoặc bằng 0")
        void exportProduct_WhenQuantityInvalid_ShouldThrowException() {

            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(1);
            request.setQuantity(0);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> stockMovementService.exportProduct(request, authentication)
            );

            assertEquals("Số lượng xuất phải lớn hơn 0", exception.getMessage());

            // Verify
            verifyNoInteractions(
                    productRepository,
                    userRepository,
                    stockRepository,
                    auditLogService,
                    stockMovementMapper
            );
        }

        @Test
        @DisplayName("Kiểm tra xuất kho thất bại khi không tìm thấy sản phẩm")
        void exportProduct_WhenProductNotFound_ShouldThrowException() {

            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(99);
            request.setQuantity(20);

            when(productRepository.findById(99))
                    .thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> stockMovementService.exportProduct(request, authentication)
            );

            assertEquals("Không tìm thấy sản phẩm cần xuất hàng", exception.getMessage());

            // Verify
            verify(productRepository).findById(99);

            verifyNoInteractions(
                    userRepository,
                    stockRepository,
                    auditLogService,
                    stockMovementMapper
            );
        }

        @Test
        @DisplayName("Kiểm tra xuất kho thất bại khi số lượng xuất vượt tồn kho")
        void exportProduct_WhenQuantityExceedsStock_ShouldThrowException() {

            // Arrange
            StockMovementRequestDTO request = new StockMovementRequestDTO();
            request.setProductId(1);
            request.setQuantity(150);

            when(productRepository.findById(1))
                    .thenReturn(Optional.of(product));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> stockMovementService.exportProduct(request, authentication)
            );

            assertEquals(
                    "Số lượng xuất vượt quá số lượng tồn kho hiện tại",
                    exception.getMessage()
            );

            // Verify
            verify(productRepository).findById(1);

            verifyNoInteractions(
                    userRepository,
                    stockRepository,
                    auditLogService,
                    stockMovementMapper
            );
        }
    }

    @Nested
    @DisplayName("History import/export")
    class HistoryTest {
        @Test
        @DisplayName("Lấy lịch sử nhập kho thành công")
        void getStockMovements_WhenTypeIn_ShouldReturnHistorySuccessfully() {
            // Arrange
            when(stockRepository.findByType("IN"))
                    .thenReturn(List.of(stockMovement));

            when(stockMovementHistoryMapper.toResponse(stockMovement))
                    .thenReturn(historyResponseDTO);

            // Act
            List<StockMovementHistoryResponseDTO> result =
                    stockMovementService.getStockMovements("IN");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            // Verify
            verify(stockRepository).findByType("IN");
            verify(stockMovementHistoryMapper).toResponse(stockMovement);
        }

        @Test
        @DisplayName("Lấy lịch sử xuất kho thành công")
        void getStockMovements_WhenTypeOut_ShouldReturnHistorySuccessfully() {
            // Arrange
            when(stockRepository.findByType("OUT"))
                    .thenReturn(List.of(stockMovement));

            when(stockMovementHistoryMapper.toResponse(stockMovement))
                    .thenReturn(historyResponseDTO);

            // Act
            List<StockMovementHistoryResponseDTO> result =
                    stockMovementService.getStockMovements("OUT");

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            // Verify
            verify(stockRepository).findByType("OUT");
            verify(stockMovementHistoryMapper).toResponse(stockMovement);
        }
    }


}
