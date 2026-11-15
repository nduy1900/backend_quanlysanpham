package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.category.CategoryRequestDTO;
import com.example.Quanlysanpham.dto.response.category.CategoryResponseDTO;
import com.example.Quanlysanpham.entity.Category;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.CategoryMapper;
import com.example.Quanlysanpham.repository.CategoryRepository;
import com.example.Quanlysanpham.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1);
        category.setName("Smartphone");
        category.setDescription("Điện thoại thông minh");

        responseDTO = new CategoryResponseDTO();
        responseDTO.setId(1);
        responseDTO.setName("Smartphone");
        responseDTO.setDescription("Điện thoại thông minh");
    }

    // getAll: Lấy danh sách
    @Nested
    @DisplayName("getAll")
    class GetAllTest {
        @Test
        @DisplayName("Kiểm tra lấy danh sách danh mục thành công")
        void getAll_ShouldReturnGetListSuccessfully() {
            // Arrange
            when(categoryRepository.findAll()).thenReturn(List.of(category));
            when(categoryMapper.toResponse(category)).thenReturn(responseDTO);

            // Act
            List<CategoryResponseDTO> result = categoryService.getAll();

            // Assert
            assertNotNull(result);
            assertEquals("Smartphone", result.get(0).getName());
            assertEquals("Điện thoại thông minh", result.get(0).getDescription());
            verify(categoryRepository).findAll();
            verify(categoryMapper).toResponse(category);
        }

        @Test
        @DisplayName("Kiểm tra lấy danh sách danh mục rỗng")
        void getAll_WhenNoCategories_ShouldReturnEmptyList() {
            when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

            List<CategoryResponseDTO> result = categoryService.getAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(categoryRepository).findAll();
            verifyNoInteractions(categoryMapper);
        }
    }

    // getCategoryById: Lấy danh mục theo id
    @Nested
    @DisplayName("getCategoryById")
    class GetCategoryByIdTest {
        @Test
        @DisplayName("Kiểm tra lấy danh mục theo id thành công")
        void getCategoryById_ShouldReturnCategoryById() {
            // Arrange
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            when(categoryMapper.toResponse(category)).thenReturn(responseDTO);

            // Act
            CategoryResponseDTO result = categoryService.getCategoryById(1);

            // Assert
            assertNotNull(result);
            assertSame(responseDTO, result);
            assertEquals(1, result.getId());
            verify(categoryRepository).findById(1);
            verify(categoryMapper).toResponse(category);

        }

        @Test
        @DisplayName("Kiểm tra không tồn tại danh mục chứa ID")
        void getCategoryById_WhenCategoryNotExists_ShouldThrowException() {
            // Arrange
            when(categoryRepository.findById(88)).thenReturn(Optional.empty());

            // Assert
            assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(88));
            verify(categoryRepository).findById(88);
            verifyNoInteractions(categoryMapper);
        }
    }

    // createCategory: Tạo danh mục
    @Nested
    @DisplayName("createCategory")
    class CreateCategoryTest {
        @Test
        @DisplayName("Kiểm tra tạo danh mục thành công")
        void createCategory_ShouldReturnSuccessfully() {
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Smartphone");
            request.setDescription("Điện thoại thông minh");

            Category savedCategory = new Category();
            savedCategory.setId(1);
            savedCategory.setName("Smartphone");
            savedCategory.setDescription("Điện thoại thông minh");

            when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
            when(categoryMapper.toResponse(savedCategory)).thenReturn(responseDTO);
            when(authentication.getName()).thenReturn("admin");

            CategoryResponseDTO result = categoryService.createCategory(request, authentication);

            assertNotNull(result);
            assertSame(responseDTO, result);
            assertEquals("Smartphone", result.getName());
            assertEquals("Điện thoại thông minh", result.getDescription());

            verify(categoryRepository).save(any(Category.class));
            verify(categoryMapper).toResponse(savedCategory);
            verify(auditLogService).saveLog("admin", "CREATE", "Category", 1);

        }
    }

    // updateCategory: Cập nhật danh mục
    @Nested
    @DisplayName("updateCategory")
    class UpdateCategory {
        @Test
        @DisplayName("Kiểm tra cập nhật danh mục thành công")
        void updateCategory_ShouldReturnUpdatedCategorySuccessfully() {
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Smartphone");
            request.setDescription("Điện thoại thông minh");

            Category updatedCategory = new Category();
            updatedCategory.setId(1);
            updatedCategory.setName(request.getName());
            updatedCategory.setDescription(request.getDescription());

            when(authentication.getName()).thenReturn("admin");
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
            when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
            when(categoryMapper.toResponse(updatedCategory)).thenReturn(responseDTO);

            CategoryResponseDTO result = categoryService.updateCategory(1, request, authentication);

            assertNotNull(result);
            assertSame(responseDTO, result);
            verify(categoryRepository).save(any(Category.class));
            verify(categoryMapper).toResponse(updatedCategory);
            verify(auditLogService).saveLog("admin", "UPDATE", "Category", 1);


        }

        @Test
        @DisplayName("Kiểm tra cập nhật danh mục không tồn tại")
        void updateCategory_WhenCategoryNotExists_ShouldThrowException() {
            CategoryRequestDTO request = new CategoryRequestDTO();
            request.setName("Smartphone");
            request.setDescription("Điện thoại thông minh");

            when(categoryRepository.findById(99)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.updateCategory(99, request, authentication));

            verify(categoryRepository, never()).save(any(Category.class));
            verifyNoInteractions(categoryMapper);
            verifyNoInteractions(auditLogService);
        }
    }

    // deleteCategory
    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {
        @Test
        @DisplayName("Kiểm tra xoá danh mục thành công")
        void deleteCategory_ShouldReturnDeletedCategorySuccessfully() {
            when(authentication.getName()).thenReturn("admin");
            when(categoryRepository.findById(1)).thenReturn(Optional.of(category));

            categoryService.deleteCategory(1, authentication);

            verify(categoryRepository).delete(category);
            verify(auditLogService).saveLog("admin", "DELETE", "Category", 1);
        }

        @Test
        @DisplayName("Kiểm tra xoá danh mục không tồn tại")
        void deleteCategory_WhenCategoryNotExists_ShouldThrowException() {
            when(categoryRepository.findById(99)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> categoryService.deleteCategory(99, authentication));

            verify(categoryRepository, never()).delete(any(Category.class));
            verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("Kiểm tra không thể xoá danh mục nếu tồn tại sản phẩm")
        void deleteCategory_WhenCategoryHasProducts_ShouldThrowException() {
            // Arrange
            int categoryId = 1;

            when(categoryRepository.findById(categoryId))
                    .thenReturn(Optional.of(category));

            when(productRepository.existsByCategory_Id(categoryId))
                    .thenReturn(true);

            // Act & Assert
            assertThrows(
                    DuplicateResourceException.class,
                    () -> categoryService.deleteCategory(categoryId, authentication)
            );

            // Verify
            verify(categoryRepository).findById(categoryId);
            verify(productRepository).existsByCategory_Id(categoryId);
            verify(categoryRepository, never()).delete(any(Category.class));
            verifyNoInteractions(auditLogService);
        }
    }
}
