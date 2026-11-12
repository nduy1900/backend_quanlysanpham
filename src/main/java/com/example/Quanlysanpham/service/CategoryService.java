package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.category.CategoryRequestDTO;
import com.example.Quanlysanpham.dto.response.category.CategoryResponseDTO;
import com.example.Quanlysanpham.entity.Category;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.CategoryMapper;
import com.example.Quanlysanpham.repository.CategoryRepository;
import com.example.Quanlysanpham.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    // Lấy danh sách

    public List<CategoryResponseDTO> getAll() {
        logger.info("API được gọi: CategoryController.getCategoryList()");
        List<Category> categories = categoryRepository.findAll();
        logger.info("Số lượng danh mục trong danh sách là: " + categories.size());
        return categories
                .stream()
                .map(categoryMapper::toResponse).toList();
    }

    // Lấy theo id
    public CategoryResponseDTO getCategoryById(int id) {
        logger.info("API được gọi: CategoryController.getCategoryById()");
        logger.info("Bắt đầu tìm danh mục có ID: " + id);
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy danh mục có ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy danh mục có id = " + id);
                }
        );
        logger.info("Tìm thấy sản phẩm " + category.getName());
        return categoryMapper.toResponse(category);
    }

    // Tạo danh mục
    public CategoryResponseDTO createCategory(CategoryRequestDTO request, Authentication authentication) {
        logger.info("API được gọi: CategoryController.postCategory()");
        logger.info("Bắt đầu tạo sản phẩm: " + request.getName());
        Category newCategory = new Category();
        newCategory.setName(request.getName());
        newCategory.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(newCategory);
        logger.info("Thêm sản phẩm thành công, ID: " + savedCategory.getId());
        // Ghi lịch sử tạo danh mục
        auditLogService.saveLog(authentication.getName(), "CREATE", "Category", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    // Cập nhật danh mục
    public CategoryResponseDTO updateCategory(int id, CategoryRequestDTO request, Authentication authentication) {
        logger.info("API được gọi: CategoryController.putCategory()");
        Category updatedCategory = categoryRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy danh mục, ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy danh mục có id = " + id);
                }
        );

        updatedCategory.setName(request.getName());
        updatedCategory.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(updatedCategory);
        logger.info("Cập nhật danh mục thành công, ID: " + id);
        // Ghi lịch sử cập nhật danh mục
        auditLogService.saveLog(authentication.getName(), "UPDATE", "Category", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    // Xoá danh mục
    public void deleteCategory(int id, Authentication authentication) {
        Category deletedCategory = categoryRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy danh mục, ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy danh mục có id = " + id);
                }
        );

        //  Kiểm tra Category có sản phẩm không
        if (productRepository.existsByCategory_Id(id)) {
            throw new DuplicateResourceException(
                    "Không thể xóa danh mục đang có sản phẩm"
            );
        }
        categoryRepository.delete(deletedCategory);
        logger.info("Xoá danh mục thành công, ID: " + id);
        // Ghi lịch sử xoá danh mục
        auditLogService.saveLog(authentication.getName(), "DELETE", "Category", deletedCategory.getId());
    }
}
