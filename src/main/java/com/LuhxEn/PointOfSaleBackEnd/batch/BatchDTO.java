package com.LuhxEn.PointOfSaleBackEnd.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BatchDTO {
	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class WastageProducts{
		private long wastedStocks;
		private double wastedSum;
	}
}
