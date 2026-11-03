package com.example.Quanlysanpham.repository;

import com.example.Quanlysanpham.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductId(int productId);

    boolean existsByProductId(int productId);
}
