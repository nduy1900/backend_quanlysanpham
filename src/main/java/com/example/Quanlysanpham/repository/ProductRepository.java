package com.example.Quanlysanpham.repository;

import com.example.Quanlysanpham.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, int id);

    // Kiểm tra Category có sản phẩm hay không
    boolean existsByCategory_Id(int categoryId);

    // Kiểm tra Brand có sản phẩm hay không
    boolean existsByBrand_Id(int brandId);

    // Kiểm tra sản phẩm còn hàng không
    @Query("""
                    SELECT p FROM Product p WHERE p.quantity <= p.minStock
                    AND p.deleted = false
                    ORDER BY p.quantity ASC
            """)
    List<Product> findLowStockProducts();

    // Tìm các sản phẩm theo Sku
    List<Product> findBySkuContainingIgnoreCase(String sku);

    // Tìm các sản phẩm theo danh mục
    @Query("""
                SELECT c.id, c.name, COUNT(p.id)
                FROM Category c
                LEFT JOIN Product p
                    ON p.category.id = c.id
                    AND p.deleted = false
                GROUP BY c.id, c.name
                ORDER BY c.id ASC
            """)
    List<Object[]> findProductByCategory();
}
