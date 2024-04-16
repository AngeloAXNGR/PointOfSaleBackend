package com.LuhxEn.PointOfSaleBackEnd.sale;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

public class SaleDTO {
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ProductList {
		private Long productId;
		private String productName;
		private int quantity;
		private double subtotal;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SaleRequest {
		@NotNull
		private Long productId;
		private int quantity;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class SaleResponse {
		private Long saleId;
		private List<ProductList> products;
		private double grandTotal;
		private Date transactionDate;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DailyTotalSaleAmount{
		private double dailyTotalSaleAmount;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MonthlyTotalSaleAmount{
		private double monthlyTotalSaleAmount;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DailyTotalProductsSold{
		private int dailyTotalProductsSold;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MonthlyTotalProductsSold{
		private int monthlyTotalProductsSold;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MonthlyTotalSoldForTheYear{
		private int monthlyTotalProductsSold;
		private int month;
		private int year;
	}



	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Dashboard{
		private double dailyTotalSaleAmount;
		private double monthlyTotalSaleAmount;
		private int dailyTotalProductsSold;
		private int monthlyTotalProductSold;
	}

}
