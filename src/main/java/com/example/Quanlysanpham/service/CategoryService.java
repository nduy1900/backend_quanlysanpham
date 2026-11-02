package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.category.CategoryRequestDTO;
import com.example.Quanlysanpham.dto.response.category.CategoryResponseDTO;
import com.example.Quanlysanpham.entity.Category;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.CategoryMapper;
import com.example.Quanlysanpham.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // Lấy danh sách
    public List<CategoryResponseDTO> getAll() {
        List<Category> categories = categoryRepository.findAll();
        return categories
                .stream()
                .map(categoryMapper::toResponse).toList();
    }

    // Lấy theo id
    public CategoryResponseDTO getCategoryById(int id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy danh mục có id = " + id)
        );
        return categoryMapper.toResponse(category);
    }

    // Tạo danh mục
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category newCategory = new Category();
        newCategory.setName(request.getName());
        newCategory.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(newCategory);
        return categoryMapper.toResponse(savedCategory);
    }

    // Cập nhật danh mục
    public CategoryResponseDTO updateCategory(int id, CategoryRequestDTO request) {
        Category updatedCategory = categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy danh mục có id = " + id)
        );

        updatedCategory.setName(request.getName());
        updatedCategory.setDescription(request.getDescription());

        Category savedCategory = categoryRepository.save(updatedCategory);
        return categoryMapper.toResponse(savedCategory);
    }

    // Xoá danh mục
    public void deleteCategory(int id) {
        Category deletedCategory = categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy danh mục có id = " + id)
        );
        categoryRepository.delete(deletedCategory);
    }
}
