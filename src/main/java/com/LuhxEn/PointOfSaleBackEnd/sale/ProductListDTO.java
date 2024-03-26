package com.LuhxEn.PointOfSaleBackEnd.sale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDTO {
	private Long productId;
	private String productName;
	private int quantity;
	private double subtotal;
}
