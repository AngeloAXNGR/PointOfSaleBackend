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
	public static class DailyTotalRevenue{
		private double dailyTotalRevenue;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MonthlyTotalRevenue{
		private double monthlyTotalRevenue;
	}


	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AnnualTotalRevenue{
		private double annualTotalRevenue;
	}



	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MonthlyRevenuesForTheYear{
		private double monthlyRevenuesForTheYear;
		private int month;
		private int year;
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
	public static class AnnualTotalProductsSold{
		private int annualTotalProductsSold;
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
		private double dailyTotalRevenue;
		private double monthlyTotalRevenue;
		private int dailyTotalProductsSold;
		private int monthlyTotalProductSold;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class PopularProductDTO{
		private String productName;
		private Long quantitySold;

	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DailyProfit{
		private double dailyProfit;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MonthlyProfit{
		private double monthlyProfit;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class AnnualProfit{
		private double annualProfit;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RevenueOverview{
		private double dailyTotalRevenue;
		private double monthlyTotalRevenue;
		private double annualTotalRevenue;
		private double dailyProfit;
		private double monthlyProfit;
		private double annualProfit;
	}


}
