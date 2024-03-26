package com.LuhxEn.PointOfSaleBackEnd.sale;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaleRequestDTO {
	@NotNull
	private Long productId;
	private int quantity;
}
