package com.LuhxEn.PointOfSaleBackEnd.product;

import com.LuhxEn.PointOfSaleBackEnd.batch.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
	@Query(value = "SELECT * FROM Product WHERE business_id = :businessId AND is_deleted = false ORDER BY product_name", nativeQuery = true)
	List<Product> getProductsAsc(@Param("businessId") Long businessId);
	@Query(value = "SELECT * FROM Product WHERE LOWER(product_name) = LOWER(:productName) AND is_deleted = false", nativeQuery = true)
	Product findByProductNameIgnoreCase(String productName);
}
