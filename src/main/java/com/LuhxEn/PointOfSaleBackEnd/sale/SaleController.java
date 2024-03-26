package com.LuhxEn.PointOfSaleBackEnd.sale;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {
	private final SaleService saleService;

	@PostMapping("/{businessId}/create")
	public ResponseEntity<SaleResponseDTO> createSale(@PathVariable Long businessId, @RequestBody List<SaleRequestDTO> saleRequestDTOs){
		return saleService.createSale(businessId, saleRequestDTOs);
	}
}
