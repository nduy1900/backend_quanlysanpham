package com.example.Quanlysanpham.repository;

import com.example.Quanlysanpham.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, int id);

    // Kiểm tra Category có sản phẩm hay không
    boolean existsByCategory_Id(int categoryId);

    // Kiểm tra Brand có sản phẩm hay không
    boolean existsByBrand_Id(int brandId);
}
