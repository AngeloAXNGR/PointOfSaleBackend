package com.LuhxEn.PointOfSaleBackEnd.product;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

public class ProductDTO {
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ProductRequest{
		@NotNull
		private Long productId;
		private String productName;
		private Long categoryId;
		private double purchasePrice;
		private double sellingPrice;
		private int stock;
		private int lowStockThreshold;
		private Date expirationDate;
		private boolean isDeleted;

	}
}
