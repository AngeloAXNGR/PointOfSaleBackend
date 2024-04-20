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

	@GetMapping("/{businessId}/dailyTotalSaleAmount")
	public ResponseEntity<SaleDTO.DailyTotalSaleAmount>  getDailyTotalSaleAmount(@PathVariable Long businessId){
		return saleService.getDailyTotalSaleAmount(businessId);
	}

	@GetMapping("/{businessId}/monthlyTotalSaleAmount")
	public ResponseEntity<SaleDTO.MonthlyTotalSaleAmount> getMonthlyTotalSaleAmount(@PathVariable Long businessId){
		return saleService.getMonthlyTotalSaleAmount(businessId);
	}

	@GetMapping("/{businessId}/monthlySaleForTheYear")
	public ResponseEntity<List<SaleDTO.MonthlySaleForTheYear>> getMonthlySaleForYear(@PathVariable Long businessId){
		return saleService.getMonthlySaleForYear(businessId);
	}


	@GetMapping("/{businessId}/dailyTotalProductsSold")
	public ResponseEntity<SaleDTO.DailyTotalProductsSold> getDailyTotalProductsSold(@PathVariable Long businessId){
		return saleService.getDailyTotalProductsSold(businessId);
	}

	@GetMapping("/{businessId}/monthlyTotalProductsSold")
	public ResponseEntity<SaleDTO.MonthlyTotalProductsSold> getMonthlyTotalProductsSold(@PathVariable Long businessId){
		return saleService.getMonthlyTotalProductsSold(businessId);
	}


	@GetMapping("/{businessId}/monthlySoldForTheYear")
	public ResponseEntity<List<SaleDTO.MonthlyTotalSoldForTheYear>> getMonthlyTotalSoldForYear(@PathVariable Long businessId){
		return saleService.getMonthlyTotalSoldForYear(businessId);
	}


	@GetMapping("/{businessId}/dashboard")
	public ResponseEntity<SaleDTO.Dashboard> getDashboardValues(@PathVariable Long businessId){
		return saleService.getDashboardValues(businessId);
	}

	@GetMapping("/{businessId}/popularProducts")
	public ResponseEntity<List<SaleDTO.PopularProductDTO>> getMostPopularProducts(@PathVariable Long businessId){
		return saleService.getMostPopularProducts(businessId);
	}

	@GetMapping("/{businessId}/monthlyProfit")
	public ResponseEntity<SaleDTO.Profit> getMonthlyProfit(@PathVariable Long businessId){
		return saleService.getMonthlyProfit(businessId);
	}
}
