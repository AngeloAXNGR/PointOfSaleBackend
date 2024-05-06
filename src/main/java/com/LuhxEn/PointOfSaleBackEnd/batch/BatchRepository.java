package com.LuhxEn.PointOfSaleBackEnd.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

	@Query(value = "SELECT * FROM batch WHERE product_id = :productId", nativeQuery = true)
	List<Batch> getBatch(Long productId);


	@Query(value = "SELECT * FROM batch WHERE product_id = :productId AND DATE(expiration_date) >= CURRENT_DATE AND stock > 0 ORDER BY expiration_date ASC", nativeQuery = true)
	List<Batch> getNonExpiredBatch(Long productId);


	@Query(value = "SELECT COALESCE(SUM(stock),0) FROM batch WHERE business_id = :businessId AND DATE(expiration_date) >= CURRENT_DATE AND stock > 0", nativeQuery = true)
	Integer getTotalStocks(Long businessId);

	@Query(value = "SELECT COALESCE(SUM(stock),0), COALESCE(SUM(stock * batch_purchase_price),0) FROM batch WHERE business_id = :businessId AND DATE(expiration_date) <= CURRENT_DATE AND stock > 0", nativeQuery = true)
	Object getWastageProducts(Long businessId);
}
