package com.LuhxEn.PointOfSaleBackEnd.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

public class BatchDTO {
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class WastageProducts{
		private long wastedStocks;
		private double wastedSum;
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RestockProduct{
		private Long businessId;
		private int stock;
		private Date expirationDate;
	}
}
