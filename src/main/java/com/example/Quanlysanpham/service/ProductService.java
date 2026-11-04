package com.example.Quanlysanpham.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.Quanlysanpham.dto.request.product.ProductRequestDTO;
import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.dto.response.product_image.ProductImageResponseDTO;
import com.example.Quanlysanpham.entity.*;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.ProductMapper;
import com.example.Quanlysanpham.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final Cloudinary cloudinary;

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

    // Upload ảnh sản phẩm
    public ProductImageResponseDTO uploadImageProduct(int productId, MultipartFile file) throws IOException {
        // Kiểm tra sản phẩm
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy sản phẩm")
        );

        // Kiểm tra sản phẩm có bị xoá chưa
        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Không thể upload sản phẩm vì đã bị xoá");
        }

        // Kiểm tra file
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn ảnh cần upload");
        }
        if (file.getContentType() == null ||
                !file.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File tải lên phải là hình ảnh");
        }

        String publicId = null;

        try {
            // Upload ảnh lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "products/" + productId,
                            "resource_type", "image"
                    )
            );

            // Lấy thông tin từ Cloudinary
            String url = uploadResult.get("secure_url").toString();
            publicId = uploadResult.get("public_id").toString();

            // Kiểm tra sản phẩm đã có ảnh chưa
            List<ProductImage> images = productImageRepository.findByProductId(productId);

            // Ảnh đầu tiên mặc định là ảnh đại diện
            boolean primaryImage = images.isEmpty();

            // Tạo ProductImage
            ProductImage productImage = new ProductImage();

            productImage.setUrl(url);
            productImage.setPublicId(publicId);
            productImage.setPrimaryImage(primaryImage);
            productImage.setProduct(product);

            // Lưu database
            ProductImage savedImage = productImageRepository.save(productImage);

            // Trả về DTO
            return toResponseDTO(savedImage);

        } catch (IOException e) {

            // Upload thất bại do lỗi đọc file
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể đọc file ảnh"
            );

        } catch (RuntimeException e) {

            // Nếu Cloudinary đã upload thành công
            // nhưng lưu MySQL thất bại thì xóa ảnh trên Cloudinary
            if (publicId != null) {
                try {
                    cloudinary.uploader().destroy(
                            publicId,
                            ObjectUtils.emptyMap()
                    );
                } catch (IOException cleanupException) {
                    e.addSuppressed(cleanupException);
                }
            }

            throw e;
        }
    }

    // Mapping DTO
    private ProductImageResponseDTO toResponseDTO(ProductImage image) {

        return new ProductImageResponseDTO(
                image.getId(),
                image.getUrl(),
                image.isPrimaryImage()
        );
    }

    // Xoá ảnh sản phẩm
    public void deleteImage(int imageId) {

        // 1. Tìm ảnh cần xóa
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ảnh sản phẩm"));

        // 2. Lấy danh sách ảnh cùng sản phẩm
        int productId = image.getProduct().getId();

        List<ProductImage> images = productImageRepository.findByProductId(productId);

        // 3. Xóa ảnh trên Cloudinary
        try {
            Map result = cloudinary.uploader().destroy(
                    image.getPublicId(),
                    ObjectUtils.emptyMap()
            );

            String status = result.get("result").toString();

            if (!status.equals("ok") && !status.equals("not found")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Không thể xóa ảnh trên Cloudinary"
                );
            }

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Lỗi khi kết nối Cloudinary"
            );
        }

        // 4. Kiểm tra ảnh có phải ảnh đại diện không
        boolean isPrimary = image.isPrimaryImage();

        // 5. Xóa bản ghi trong MySQL
        productImageRepository.delete(image);

        // 6. Nếu xóa ảnh đại diện thì chọn ảnh khác thay thế
        if (isPrimary) {

            images.stream()
                    .filter(item -> item.getId() != imageId)
                    .findFirst()
                    .ifPresent(nextImage -> {
                        nextImage.setPrimaryImage(true);
                        productImageRepository.save(nextImage);
                    });
        }
    }

    // KIỂM TRA SẢN PHẨM CÒN HÀNG KHÔNG
    public List<ProductResponseDTO> getLowStockProducts() {
        List<Product> productListLowStock = productRepository.findLowStockProducts();
        return productListLowStock
                .stream()
                .map(productMapper::toResponse).toList();
    }

    // Tìm sản phẩm theo mã(SKU)
    public List<ProductResponseDTO> getListProductBySku(String sku) {
        List<Product> findListProductBySku = productRepository.findBySkuContainingIgnoreCase(sku);
        return findListProductBySku
                .stream()
                .map(productMapper::toResponse).toList();
    }
}
