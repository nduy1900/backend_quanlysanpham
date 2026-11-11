package com.example.Quanlysanpham.controller;

import com.example.Quanlysanpham.dto.request.product.ProductRequestDTO;
import com.example.Quanlysanpham.dto.response.ApiResponse;
import com.example.Quanlysanpham.dto.response.product.ProductExcelResponseDTO;
import com.example.Quanlysanpham.dto.response.product.ProductResponseDTO;
import com.example.Quanlysanpham.dto.response.product_image.ProductImageResponseDTO;
import com.example.Quanlysanpham.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@EnableMethodSecurity
public class ProductController {
    private final ProductService productService;

    // LẤY DANH SÁCH SẢN PHẨM
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProductPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh sách sản phẩm thành công",
                productService.getPage(page, size)
        ));
    }

    // LẤY SẢN PHẨM THEO ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getId(@PathVariable int id) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập sản phẩm có id = " + id,
                productService.getProductById(id)
        ));
    }

    // TẠO MỚI SẢN PHẨM
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponseDTO>> postProduct(
            @Valid @RequestBody ProductRequestDTO request
            , Authentication authentication) {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Tạo sản phẩm thành công",
                productService.createProduct(request, authentication)
        ));
    }

    // CẬP NHẬT SẢN PHẨM
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> putProduct(
            @PathVariable int id,
            @Valid @RequestBody ProductRequestDTO request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Cập nhật sản phẩm thành công",
                productService.updateProduct(id, request, authentication)
        ));
    }

    // XOÁ SẢN PHẨM
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable int id, Authentication authentication) {
        productService.deleteProduct(id, authentication);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Xoá sản phẩm thành công",
                null
        ));
    }

    // UPLOAD ẢNH SẢN PHẨM
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ProductImageResponseDTO>> upload(
            @PathVariable int id,
            @RequestParam("image") MultipartFile multipartFile) throws IOException {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Upload ảnh sản phẩm thành công",
                productService.uploadImageProduct(id, multipartFile)
        ));
    }

    // XOÁ ẢNH SẢN PHẨM
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable int imageId) {
        productService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Xóa ảnh sản phẩm thành công",
                null
        ));
    }

    // TÌM SẢN PHẨM SẮP HẾT HÀNG
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> findProductLowStock() {
        return ResponseEntity.ok(new ApiResponse<>(
                200,
                "Truy cập danh sách sản phẩm sắp hết hàng",
                productService.getLowStockProducts()
        ));
    }

    // TÌM SẢN PHẨM THEO MÃ SẢN PHẨM
    @GetMapping("/keyword")
    public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> findProductBySku(@RequestParam String sku) {
        return ResponseEntity.ok(new ApiResponse(
                200,
                "Truy cập danh sách các sản phẩm theo mã " + sku,
                productService.getListProductBySku(sku)
        ));
    }

    // ĐỌC FILE EXCEL
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<List<ProductExcelResponseDTO>>> importExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(201).body(new ApiResponse<>(
                201,
                "Xem file excel thành công",
                productService.readExcel(file)

        ));
    }

    // XUẤT FILE EXCEL
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel() throws IOException {

        List<ProductResponseDTO> products = productService.getAllProducts();

        byte[] data = productService.exportExcel(products);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=products.xlsx"
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}


