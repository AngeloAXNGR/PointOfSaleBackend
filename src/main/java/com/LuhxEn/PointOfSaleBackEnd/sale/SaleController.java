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

	@GetMapping("/{businessId}/dailyTotalRevenue")
	public ResponseEntity<SaleDTO.DailyTotalRevenue>  getDailyTotalRevenue(@PathVariable Long businessId){
		return saleService.getDailyTotalRevenue(businessId);
	}

	@GetMapping("/{businessId}/monthlyTotalRevenue")
	public ResponseEntity<SaleDTO.MonthlyTotalRevenue> getMonthlyTotalRevenue(@PathVariable Long businessId){
		return saleService.getMonthlyTotalRevenue(businessId);
	}

	@GetMapping("/{businessId}/annualTotalRevenue")
	public ResponseEntity<SaleDTO.AnnualTotalRevenue> getAnnualTotalRevenue(@PathVariable Long businessId){
		return saleService.getAnnualTotalRevenue(businessId);
	}

	@GetMapping("/{businessId}/monthlyRevenuesForTheYear")
	public ResponseEntity<List<SaleDTO.MonthlyRevenuesForTheYear>> getMonthlyRevenuesForTheYear(@PathVariable Long businessId){
		return saleService.getMonthlyRevenuesForTheYear(businessId);
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
	public ResponseEntity<List<SaleDTO.MonthlyTotalSoldForTheYear>> getMonthlySoldForTheYear(@PathVariable Long businessId){
		return saleService.getMonthlySoldForTheYear(businessId);
	}


	@GetMapping("/{businessId}/dashboard")
	public ResponseEntity<SaleDTO.Dashboard> getDashboardValues(@PathVariable Long businessId){
		return saleService.getDashboardValues(businessId);
	}

	@GetMapping("/{businessId}/popularProducts")
	public ResponseEntity<List<SaleDTO.PopularProductDTO>> getMostPopularProducts(@PathVariable Long businessId){
		return saleService.getMostPopularProducts(businessId);
	}

	@GetMapping("/{businessId}/dailyProfit")
	public ResponseEntity<SaleDTO.DailyProfit> getDailyProfit(@PathVariable Long businessId){
		return saleService.getDailyProfit(businessId);
	}

	@GetMapping("/{businessId}/monthlyProfit")
	public ResponseEntity<SaleDTO.MonthlyProfit> getMonthlyProfit(@PathVariable Long businessId){
		return saleService.getMonthlyProfit(businessId);
	}

	@GetMapping("/{businessId}/annualProfit")
	public ResponseEntity<SaleDTO.AnnualProfit> getAnnualProfit(@PathVariable Long businessId){
		return saleService.getAnnualProfit(businessId);
	}
}
