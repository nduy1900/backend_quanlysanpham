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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockMovementService {
    private final StockMovementRepository stockRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockMovementMapper stockMovementMapper;
    private final StockMovementHistoryMapper stockMovementHistoryMapper;

    // Nhập sản phẩm
    @Transactional
    public StockMovementResponseDTO importProduct(StockMovementRequestDTO request, Authentication authentication) {
        // Kiểm tra số lượng nhập
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }

        // Tìm sản phẩm cần nhập trong DB
        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy sản phẩm nhập hàng")
        );
        if (product.isDeleted()) {
            throw new IllegalArgumentException(
                    "Không thể nhập kho cho sản phẩm đã bị xóa"
            );
        }

        // Tìm người nhập sản phẩm từ Access Token
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người nhập sản phẩm")
        );

        // Cập nhật số lượng tồn kho
        product.setQuantity(product.getQuantity() + request.getQuantity());

        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(user.getId());
        productRepository.save(product);


        StockMovement stockMovement = new StockMovement();
        stockMovement.setType("IN");
        stockMovement.setQuantity(request.getQuantity());
        stockMovement.setNote(request.getNote());
        stockMovement.setCreatedAt(LocalDateTime.now());
        stockMovement.setProduct(product);

        // createBy là id của người nhập sản phẩm
        stockMovement.setCreatedBy(user.getId());

        StockMovement savedStock = stockRepository.save(stockMovement);
        return stockMovementMapper.toResponse(savedStock);
    }

    // Xuất sản phẩm
    @Transactional
    public StockMovementResponseDTO exportProduct(StockMovementRequestDTO request, Authentication authentication) {
        // Kiểm tra số lượng xuất
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải lớn hơn 0");
        }

        // Tìm sản phẩm cần xuất trong DB
        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy sản phẩm cần xuất hàng")
        );
        if (product.isDeleted()) {
            throw new IllegalArgumentException(
                    "Không thể xuất kho cho sản phẩm đã bị xóa"
            );
        }

        // Kiểm tra lượng tồn kho
        if (request.getQuantity() > product.getQuantity()) {
            throw new IllegalArgumentException("Số lượng xuất vượt quá số lượng tồn kho hiện tại");
        }

        // Tìm người xuất sản phẩm từ Access Token
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người nhập sản phẩm")
        );

        // Cập nhật số lượng tồn kho
        product.setQuantity(product.getQuantity() - request.getQuantity());
        product.setUpdatedAt(LocalDateTime.now());
        product.setUpdatedBy(user.getId());
        productRepository.save(product);


        StockMovement stockMovement = new StockMovement();
        stockMovement.setType("OUT");
        stockMovement.setQuantity(request.getQuantity());
        stockMovement.setNote(request.getNote());
        stockMovement.setCreatedAt(LocalDateTime.now());
        stockMovement.setCreatedBy(user.getId());
        stockMovement.setProduct(product);

        StockMovement savedStock = stockRepository.save(stockMovement);
        return stockMovementMapper.toResponse(savedStock);
    }

    // Lịch sử nhập xuất sản phẩm
    public List<StockMovementHistoryResponseDTO> getStockMovements(String type) {
        // Kiểm tra type
        if (type != null && !type.isBlank()) {
            type = type.trim().toUpperCase();
            if (!type.equals("IN") && !type.equals("OUT")) {
                throw new IllegalArgumentException("Loại giao dịch chỉ được IN hoặc OUT");
            }
        } else {
            type = null;
        }
        
        List<StockMovement> stockMovements = stockRepository.findByType(type);
        return stockMovements
                .stream()
                .map(stockMovementHistoryMapper::toResponse).toList();
    }
}
