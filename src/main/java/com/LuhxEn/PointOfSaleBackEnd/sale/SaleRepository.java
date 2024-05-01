package com.LuhxEn.PointOfSaleBackEnd.sale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

	@Query(value = "SELECT COALESCE(SUM(s.grand_total), 0) " +
		"FROM Sale s " +
		"WHERE s.business_id = :businessId " +
		"AND DATE(s.transaction_date) = :today", nativeQuery = true)
	double getDailyTotalRevenue(@Param("businessId") Long businessId, @Param("today") LocalDate today);

	@Query(value = "SELECT COALESCE(SUM(s.grand_total), 0) " +
		"FROM Sale s " +
		"WHERE s.business_id = :businessId " +
		"AND EXTRACT(MONTH FROM s.transaction_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
		"AND EXTRACT(YEAR FROM s.transaction_date) = EXTRACT(YEAR FROM CURRENT_DATE)", nativeQuery = true)
	double getMonthlyTotalRevenue(@Param("businessId") Long businessId);

	@Query(value = "SELECT COALESCE(SUM(s.grand_total), 0) " +
		"FROM Sale s " +
		"WHERE s.business_id = :businessId " +
		"AND EXTRACT(YEAR FROM s.transaction_date) = EXTRACT(YEAR FROM CURRENT_DATE)", nativeQuery = true)
	double getAnnualTotalRevenue(@Param("businessId") Long businessId);

	@Query(value = "SELECT COALESCE(SUM(s.grand_total),0) " +
		"FROM Sale s " +
		"WHERE s.business_id = :businessId " +
		"AND EXTRACT(MONTH FROM s.transaction_date) = :month " +
		"AND EXTRACT(YEAR FROM s.transaction_date) = :year", nativeQuery = true
	)
	double getMonthlyRevenuesForTheYear(@Param("businessId") Long businessId, @Param("year") int year, @Param("month") int month);

	@Query(value = "SELECT COALESCE(SUM(sp.quantity),0) " +
		"FROM sale s " +
		"JOIN sale_products sp ON s.id = sp.sale_id " +
		"WHERE DATE(s.transaction_date) = :today " +
		"AND s.business_id = :businessId", nativeQuery = true
	)
	Integer getDailyTotalProductsSold(@Param("businessId") Long businessId, @Param("today") LocalDate today);

	@Query(value = "SELECT COALESCE(SUM(sp.quantity), 0) " +
		"FROM sale s " +
		"JOIN sale_products sp ON s.id = sp.sale_id " +
		"WHERE EXTRACT(MONTH FROM s.transaction_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
		"AND EXTRACT(YEAR FROM s.transaction_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
		"AND s.business_id = :businessId", nativeQuery = true)
	Integer getMonthlyTotalProductsSold(@Param("businessId") Long businessId);


	@Query(value = "SELECT COALESCE(SUM(sp.quantity), 0) " +
		"FROM sale s " +
		"JOIN sale_products sp ON s.id = sp.sale_id " +
		"WHERE EXTRACT(YEAR FROM s.transaction_date) = EXTRACT(YEAR FROM CURRENT_DATE) " +
		"AND s.business_id = :businessId", nativeQuery = true)
	Integer getAnnualTotalProductsSold(@Param("businessId") Long businessId);

	@Query(value = "SELECT COALESCE(SUM(sp.quantity), 0) " +
		"FROM sale s " +
		"JOIN sale_products sp ON s.id = sp.sale_id " +
		"WHERE EXTRACT(MONTH FROM s.transaction_date) = :month " +
		"AND EXTRACT(YEAR FROM s.transaction_date) = :year " +
		"AND s.business_id = :businessId", nativeQuery = true)
	Integer getMonthlySoldForTheYear(@Param("businessId") Long businessId, @Param("year") int year, @Param("month") int month);


	@Query(value = "SELECT p.*, COALESCE(SUM(sp.quantity), 0) AS total_sold " +
		"FROM Product p " +
		"JOIN sale_products sp ON p.id = sp.product_id " +
		"JOIN sale s ON sp.sale_id = s.id " +
		"WHERE s.business_id = :businessId " +
		"AND EXTRACT(MONTH FROM s.transaction_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
		"GROUP BY p.id " +
		"ORDER BY total_sold DESC " +
		"LIMIT 5", nativeQuery = true
	)
	List<Object[]> getMostPopularProducts(@Param("businessId") Long businessId);

	//	@Query(value = "SELECT SUM(sp.quantity * p.selling_price) AS sale, " +
//		"SUM(sp.quantity * p.selling_price) - SUM(sp.quantity * p.purchase_price) AS profit " +
//		"FROM sale s JOIN sale_products sp ON s.id = sp.sale_id " +
//		"JOIN product p on sp.product_id = p.id " +
//		"WHERE s.business_id = :businessId " +
//		"AND EXTRACT(MONTH FROM s.transaction_date) = EXTRACT(MONTH FROM CURRENT_DATE)"
//	, nativeQuery = true)


//	@Query(value =
//		"SELECT SUM(sp.quantity * p.selling_price) - SUM(sp.quantity * p.purchase_price) AS profit " +
//		"FROM sale s JOIN sale_products sp ON s.id = sp.sale_id " +
//		"JOIN product p on sp.product_id = p.id " +
//		"WHERE s.business_id = :businessId " +
//		"AND EXTRACT(MONTH FROM s.transaction_date) = EXTRACT(MONTH FROM CURRENT_DATE)"
//		, nativeQuery = true)

	@Query(value =
		"SELECT COALESCE(SUM(s.profit),0) " +
			"FROM sale s " +
			"WHERE s.business_id = :businessId " +
			"AND DATE(s.transaction_date) = :today"
		, nativeQuery = true)
	double getDailyProfit(@Param("businessId") Long businessId, @Param("today") LocalDate today);

	@Query(value =
		"SELECT COALESCE(SUM(s.profit),0) " +
			"FROM sale s " +
			"WHERE s.business_id = :businessId " +
			"AND EXTRACT(MONTH FROM s.transaction_date) = EXTRACT(MONTH FROM CURRENT_DATE) " +
			"AND EXTRACT(YEAR FROM s.transaction_date) = EXTRACT(YEAR FROM CURRENT_DATE)"
		, nativeQuery = true)
	Double getMonthlyProfit(@Param("businessId") Long businessId);

	@Query(value =
		"SELECT COALESCE(SUM(s.profit),0) " +
			"FROM sale s " +
			"WHERE s.business_id = :businessId " +
			"AND EXTRACT(YEAR FROM s.transaction_date) = EXTRACT(YEAR FROM CURRENT_DATE)"
		, nativeQuery = true)
	Double getAnnualProfit(@Param("businessId") Long businessId);

}