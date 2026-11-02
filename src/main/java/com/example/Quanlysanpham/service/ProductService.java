package com.example.Quanlysanpham.service;

import com.example.Quanlysanpham.dto.request.product.ProductRequestDTO;
import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.entity.Brand;
import com.example.Quanlysanpham.entity.Category;
import com.example.Quanlysanpham.entity.Product;
import com.example.Quanlysanpham.entity.User;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.ProductMapper;
import com.example.Quanlysanpham.repository.BrandRepository;
import com.example.Quanlysanpham.repository.CategoryRepository;
import com.example.Quanlysanpham.repository.ProductRepository;
import com.example.Quanlysanpham.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    // Phân trang danh sách sản phẩm
    public Page<ProductResponseDTO> getPage(int page, int size) {
//        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toResponse);
    }

    // Lấy sản phẩm theo ID
    public ProductResponseDTO getProductById(int id) {
        Product findProductInDB = productRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy sản phẩm có id = " + id)
        );
        return productMapper.toResponse(findProductInDB);
    }

    // Tạo sản phẩm
    public ProductResponseDTO createProduct(ProductRequestDTO request, Authentication authentication) {
        // 1. Lấy User đang đăng nhập từ Access Token
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy người dùng"));

        // 2. Kiểm tra SKU đã tồn tại chưa
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("SKU đã tồn tại");
        }

        // 3. Tìm Category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy danh mục"));

        // 4. Tìm Brand
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy thương hiệu"));

        // 5. Tạo Product
        Product newProduct = new Product();

        newProduct.setSku(request.getSku());
        newProduct.setName(request.getName());
        newProduct.setPrice(request.getPrice());
        newProduct.setQuantity(request.getQuantity());
        newProduct.setMinStock(request.getMinStock());
        newProduct.setStatus("ACTIVE");
        newProduct.setDeleted(false);
        newProduct.setCategory(category);
        newProduct.setBrand(brand);
        newProduct.setCreatedAt(LocalDateTime.now());

        // createdBy là id của user tạo sản phẩm
        newProduct.setCreatedBy(user.getId());

        Product savedProduct = productRepository.save(newProduct);
        return productMapper.toResponse(savedProduct);
    }

    // Cập nhật sản phẩm
    public ProductResponseDTO updateProduct(int id, ProductRequestDTO request, Authentication authentication) {
        // 1. Tìm sản phẩm cần cập nhật
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        // 2. Kiểm tra sản phẩm đã bị xóa chưa
        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Sản phẩm đã bị xóa");
        }

        // 3. Lấy User đang đăng nhập
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy người dùng"));

        // 4. Kiểm tra SKU có bị trùng không
        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new DuplicateResourceException("SKU đã tồn tại");
        }

        // 5. Tìm Category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy danh mục"));

        // 6. Tìm Brand
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Không tìm thấy thương hiệu"));

        // 7. Cập nhật thông tin sản phẩm
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setMinStock(request.getMinStock());
        product.setStatus(request.getStatus());

        product.setCategory(category);
        product.setBrand(brand);

        // 8. Cập nhật thông tin người sửa
        product.setUpdatedBy(user.getId());
        product.setUpdatedAt(LocalDateTime.now());

        // 9. Lưu database
        Product updatedProduct = productRepository.save(product);

        // 10. Mapping sang DTO
        return productMapper.toResponse(updatedProduct);

    }

    // Xoá sản phẩm
    public void deleteProduct(int id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        productRepository.delete(product);
    }
}
