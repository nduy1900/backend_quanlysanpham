package com.example.Quanlysanpham.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.Quanlysanpham.dto.request.product.ProductRequestDTO;
import com.example.Quanlysanpham.dto.request.product_supplier.ProductSupplierRequestDTO;
import com.example.Quanlysanpham.dto.response.product.ProductExcelResponseDTO;
import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.dto.response.product_image.ProductImageResponseDTO;
import com.example.Quanlysanpham.entity.*;
import com.example.Quanlysanpham.exception.DuplicateResourceException;
import com.example.Quanlysanpham.exception.ResourceNotFoundException;
import com.example.Quanlysanpham.mapper.ProductMapper;
import com.example.Quanlysanpham.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final SupplierRepository supplierRepository;
    private final AuditLogService auditLogService;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    // Phân trang danh sách sản phẩm
    public Page<ProductResponseDTO> getPage(int page, int size) {
//        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Pageable pageable = PageRequest.of(page, size);
        logger.info("Bắt đầu lấy danh sách sản phẩm");
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toResponse);
    }

    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(productMapper::toResponse).toList();
    }

    // Lấy sản phẩm theo ID
    public ProductResponseDTO getProductById(int id) {
        logger.info("Bắt đầu tìm sản phẩm với ID: {}", id);
        Product findProductInDB = productRepository.findById(id).orElseThrow(
                () -> {
                    logger.error("Không tìm thấy sản phẩm, ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy sản phẩm có id = " + id);
                }
        );
        logger.info("Tìm thấy sản phẩm: " + findProductInDB.getName());
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
        logger.info("Bắt đầu thêm sản phẩm: " + request.getName());
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

        // 6. Tạo ProductDetail
        ProductDetail productDetail = new ProductDetail();
        productDetail.setDescription(request.getProductDetail().getDescription());
        productDetail.setWeight(request.getProductDetail().getWeight());
        productDetail.setDimensions(request.getProductDetail().getDimensions());
        productDetail.setColor(request.getProductDetail().getColor());
        productDetail.setWarrantyMonths(request.getProductDetail().getWarrantyMonths());
        productDetail.setProduct(newProduct);
        newProduct.setProductDetail(productDetail);


        // 7. Tạo ProductSuppliers
        List<ProductSupplier> productSuppliers = new ArrayList<>();
        for (ProductSupplierRequestDTO supplierRequestDTO : request.getProductSuppliers()) {
            // Tìm Supplier trong database
            Supplier supplier = supplierRepository.findById(
                    supplierRequestDTO.getSupplierId()).orElseThrow(() ->
                    new ResourceNotFoundException("Không tìm thấy nhà cung cấp")
            );
            // Tạo ProductSupplier
            ProductSupplier productSupplier = new ProductSupplier();

            productSupplier.setSupplyPrice(supplierRequestDTO.getSupplyPrice());
            productSupplier.setCreatedAt(LocalDateTime.now());

            // Liên kết Product và Supplier
            productSupplier.setProduct(newProduct);
            productSupplier.setSupplier(supplier);

            productSuppliers.add(productSupplier);
        }

        newProduct.setProductSuppliers(productSuppliers);

        // 8.Lưu Product vào DB
        Product savedProduct = productRepository.save(newProduct);
        logger.info("Thêm sản phẩm thành công, ID: " + savedProduct.getId());
        // Ghi lịch sử hoạt động
        auditLogService.saveLog(authentication.getName(), "CREATE", "Product", savedProduct.getId());
        return productMapper.toResponse(savedProduct);


    }

    // Cập nhật sản phẩm
    public ProductResponseDTO updateProduct(int id, ProductRequestDTO request, Authentication authentication) {
        // 1. Tìm sản phẩm cần cập nhật
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Không tìm thấy sản phẩm, ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy sản phẩm");
                });

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

        // 9. Cập nhật ProductDetail
        // Kiểm tra Product Detail đã có chưa
        if (request.getProductDetail() != null) {
            ProductDetail updatedProductDetail = product.getProductDetail();

            // Nếu chưa có thì tạo mới Product Detail
            if (updatedProductDetail == null) {
                updatedProductDetail = new ProductDetail();
                updatedProductDetail.setProduct(product);
            }
            updatedProductDetail.setDescription(request.getProductDetail().getDescription());
            updatedProductDetail.setWeight(request.getProductDetail().getWeight());
            updatedProductDetail.setDimensions(request.getProductDetail().getDimensions());
            updatedProductDetail.setColor(request.getProductDetail().getColor());
            updatedProductDetail.setWarrantyMonths(request.getProductDetail().getWarrantyMonths());
            updatedProductDetail.setProduct(product);
            product.setProductDetail(updatedProductDetail);
        }


        // 10. Cập nhật ProductSupplier
        List<ProductSupplier> updatedProductSuppliers = new ArrayList<>();
        for (ProductSupplierRequestDTO dto : request.getProductSuppliers()) {
            Supplier supplier = supplierRepository.findById(dto.getSupplierId()).orElseThrow(
                    () -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp")
            );

            ProductSupplier updatedProductSupplier = new ProductSupplier();
            updatedProductSupplier.setSupplyPrice(dto.getSupplyPrice());
            updatedProductSupplier.setSupplier(supplier);
            updatedProductSupplier.setProduct(product);
            updatedProductSupplier.setCreatedAt(LocalDateTime.now());

            updatedProductSuppliers.add(updatedProductSupplier);
        }
        product.setProductSuppliers(updatedProductSuppliers);

        // 11. Lưu database
        Product updatedProduct = productRepository.save(product);
        logger.info("Cập nhật sản phẩm thành công, ID: " + updatedProduct.getId());
        // Ghi lịch sử hoạt động
        auditLogService.saveLog(authentication.getName(), "UPDATE", "Product", updatedProduct.getId());

        // 12. Mapping sang DTO
        return productMapper.toResponse(updatedProduct);

    }

    // Xoá sản phẩm
    public void deleteProduct(int id, Authentication authentication) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Không tìm thấy sản phẩm với ID: " + id);
                    return new ResourceNotFoundException("Không tìm thấy sản phẩm");
                });
        productRepository.delete(product);
        logger.info("Xoá sản phẩm thành công, ID: " + id);
        // Ghi lịch sử xoá sản phẩm
        auditLogService.saveLog(authentication.getName(), "DELETE", "Product", product.getId());
    }

    // Upload ảnh sản phẩm
    public ProductImageResponseDTO uploadImageProduct(int productId, MultipartFile file) throws IOException {
        logger.info("Bắt đầu upload ảnh của sản phẩm có ID = " + productId);
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
            logger.info("Upload ảnh thành công");
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
                .orElseThrow(() -> {
                    logger.error("Không tìm thấy ảnh sản phẩm, ID: " + imageId);
                    return new ResourceNotFoundException("Không tìm thấy ảnh sản phẩm");
                });

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
        logger.info("Xoá ảnh thành công");

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
        logger.info("API được gọi: ProductController.findProductLowStock()");
        List<Product> productListLowStock = productRepository.findLowStockProducts();
        return productListLowStock
                .stream()
                .map(productMapper::toResponse).toList();
    }

    // Tìm sản phẩm theo mã(SKU)
    public List<ProductResponseDTO> getListProductBySku(String sku) {
        logger.info("API được gọi: ProductController.findProductBySku()");
        List<Product> findListProductBySku = productRepository.findBySkuContainingIgnoreCase(sku);
        return findListProductBySku
                .stream()
                .map(productMapper::toResponse).toList();
    }

    // Xem file excel
    public List<ProductExcelResponseDTO> readExcel(MultipartFile file) throws IOException {
        logger.info("API được gọi: ProductController.importExcel()");
        List<ProductExcelResponseDTO> excelResponseDTOList = new ArrayList<>();
        // Tạo Workbook
        Workbook workbook = WorkbookFactory.create(file.getInputStream());

        // Chọn trang sheet
        Sheet sheet = workbook.getSheetAt(0);

        // Lặp vòng lặp từng hàng
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            // Kiểm tra row có null hay không
            if (row == null) {
                continue;
            }
            // Lấy value của từng ô trong hàng
            String idValue = getCellValue(row.getCell(0));
            String sku = getCellValue(row.getCell(1));
            String name = getCellValue(row.getCell(2));
            String priceValue = getCellValue(row.getCell(3));
            String quantityValue = getCellValue(row.getCell(4));
            String minStockValue = getCellValue(row.getCell(5));
            String status = getCellValue(row.getCell(6));
            String deletedValue = getCellValue(row.getCell(7));


            // Chuyển đổi kiểu dữ liệu
            int id = Integer.parseInt(idValue);
            double price = Double.parseDouble(priceValue);
            int quantity = Integer.parseInt(quantityValue);
            int minStock = Integer.parseInt(minStockValue);
            boolean deleted = Boolean.parseBoolean(deletedValue);


            // Tạo đối tượng
            ProductExcelResponseDTO dto = new ProductExcelResponseDTO(
                    id, sku, name, price, quantity, minStock, status, deleted
            );
            // Thêm vào danh sách
            excelResponseDTOList.add(dto);
        }
        // Đóng Workbook
        workbook.close();

        return excelResponseDTOList;
    }

    // Do không biết kiểu dữ liệu là gì, có thể là chuỗi,
    // có thể là số,… phải có phương thức đọc dữ liệu
    private String getCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        return new DataFormatter()
                .formatCellValue(cell);
    }

    // Xuất file Excel
    public byte[] exportExcel(List<ProductResponseDTO> products) throws IOException {
        logger.info("API được gọi: ProductController.exportExcel()");
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Products");

            // 1. Tạo tiêu đề
            String[] headers = {
                    "ID",
                    "SKU",
                    "Name",
                    "Price",
                    "Quantity",
                    "MinStock",
                    "Status",
                    "Deleted",
                    "CreatedAt",
                    "UpdatedAt",
                    "CreatedBy",
                    "UpdatedBy",
                    "Category",
                    "Brand",
                    "Description",
                    "Weight",
                    "Dimensions",
                    "Color",
                    "WarrantyMonths"
            };

            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // 2. Ghi dữ liệu
            int rowIndex = 1;

            for (ProductResponseDTO product : products) {

                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getSku());
                row.createCell(2).setCellValue(product.getName());
                row.createCell(3).setCellValue(product.getPrice());
                row.createCell(4).setCellValue(product.getQuantity());
                row.createCell(5).setCellValue(product.getMinStock());
                row.createCell(6).setCellValue(product.getStatus());
                row.createCell(7).setCellValue(product.isDeleted());

                // Thời gian
                row.createCell(8).setCellValue(
                        product.getCreatedAt() != null ? product.getCreatedAt().toString() : "");

                row.createCell(9).setCellValue(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : "");

                row.createCell(10).setCellValue(product.getCreatedBy());

                row.createCell(11).setCellValue(product.getUpdatedBy());

                // Category
                row.createCell(12).setCellValue(product.getCategory() != null
                        ? product.getCategory().getName() : ""
                );

                // Brand
                row.createCell(13).setCellValue(product.getBrand() != null
                        ? product.getBrand().getName() : ""
                );

                // ProductDetail
                if (product.getProductDetail() != null) {

                    row.createCell(14).setCellValue(
                            product.getProductDetail().getDescription() != null
                                    ? product.getProductDetail().getDescription() : ""
                    );

                    row.createCell(15).setCellValue(
                            product.getProductDetail().getWeight()
                    );

                    row.createCell(16).setCellValue(
                            product.getProductDetail().getDimensions() != null
                                    ? product.getProductDetail().getDimensions() : ""
                    );

                    row.createCell(17).setCellValue(
                            product.getProductDetail().getColor() != null
                                    ? product.getProductDetail().getColor() : ""
                    );

                    row.createCell(18).setCellValue(
                            product.getProductDetail().getWarrantyMonths()
                    );
                }
            }

            // 3. Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 4. Ghi Workbook vào OutputStream
            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

}



