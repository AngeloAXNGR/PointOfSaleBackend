package com.LuhxEn.PointOfSaleBackEnd.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface SaleRepository extends JpaRepository<Sale, Long> {

	@Query(value = "SELECT COALESCE(SUM(s.grand_total), 0) " +
		"FROM Sale s " +
		"WHERE s.business_id = :businessId " +
		"AND DATE(s.transaction_date) = :today", nativeQuery = true)
	double getTotalSaleAmountForToday(@Param("businessId") Long businessId, @Param("today") LocalDate today);
}
