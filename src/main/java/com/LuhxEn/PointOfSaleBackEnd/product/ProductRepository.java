package com.LuhxEn.PointOfSaleBackEnd.product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {
	@Query(value = "SELECT * FROM Product WHERE business_id = :businessId AND is_deleted = false ORDER BY product_name", nativeQuery = true)
	List<Product> getProductsAsc(@Param("businessId") Long businessId);
	@Query(value = "SELECT * FROM Product WHERE LOWER(product_name) = LOWER(:productName) AND is_deleted = false AND business_id= :businessId", nativeQuery = true)
	Product findByProductNameIgnoreCase(String productName, Long businessId);

	@Query(value = "SELECT * FROM Product WHERE business_id = :businessId " +
		"AND is_deleted = false " +
		"AND (LOWER(:keyword) IS NULL OR LOWER(product_name) LIKE CONCAT(LOWER(:keyword), '%')) " +
		"ORDER BY product_name",
		nativeQuery = true)
	Page<Product> getProductsPaginated(@Param("businessId") Long businessId,@Param("keyword")String keyword, Pageable pageable);
}
