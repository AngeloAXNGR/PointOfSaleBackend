package com.LuhxEn.PointOfSaleBackEnd.sale;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleResponseDTO {
	private Long saleId;
	private List<ProductListDTO> products;
	private double grandTotal;
	private Date transactionDate;
}
