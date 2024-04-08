package com.LuhxEn.PointOfSaleBackEnd.sale;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SaleController {
	private final SaleService saleService;

	@PostMapping("/{businessId}/create")
	public ResponseEntity<?> createSale(@PathVariable Long businessId, @RequestBody List<SaleDTO.SaleRequest> saleRequestDTOs){
		return saleService.createSale(businessId, saleRequestDTOs);
	}

	@GetMapping("/{businessId}")
	public ResponseEntity<List<SaleDTO.SaleResponse>> getAllSales(@PathVariable Long businessId) {
		return saleService.getAllSales(businessId);
	}

//	@GetMapping("/{businessId}/est")
//	public ResponseEntity<List<SaleDTO.SaleResponse>>  getSalesForToday(@PathVariable Long businessId){
//		return saleService.getSalesForToday(businessId);
//	}

	@GetMapping("/{businessId}/todayTotalSaleAmount")
	public ResponseEntity<SaleDTO.TodayTotalSaleAmount>  getTodayTotalSaleAmount(@PathVariable Long businessId){
		return saleService.getTodayTotalSaleAmount(businessId);
	@GetMapping("/{businessId}/dailyTotalSaleAmount")
	public ResponseEntity<SaleDTO.DailyTotalSaleAmount>  getDailyTotalSaleAmount(@PathVariable Long businessId){
		return saleService.getDailyTotalSaleAmount(businessId);
	}
	}
}
