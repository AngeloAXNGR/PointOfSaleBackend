package com.LuhxEn.PointOfSaleBackEnd.sale;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origin}")
public class SaleController {
	private final SaleService saleService;

	@PostMapping("/{businessId}/create")
	public ResponseEntity<?> createSale(@PathVariable Long businessId, @RequestBody SaleDTO.SaleRequest saleRequestDTO){
		return saleService.createSale(businessId, saleRequestDTO);
	}

	@GetMapping("/{businessId}")
	public ResponseEntity<SaleResponse> getSalesPaginated(@PathVariable Long businessId, int page, int size) {
		return saleService.getSalesPaginated(businessId, page, size);
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

	@GetMapping("/{businessId}/monthlyRevenueAndProfitForTheYear")
	public ResponseEntity<List<SaleDTO.MonthlyRevenueAndProfits>> getMonthlyRevenuesForTheYear(@PathVariable Long businessId){
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

	@GetMapping("/{businessId}/annualTotalProductsSold")
	public ResponseEntity<SaleDTO.AnnualTotalProductsSold> getAnnualTotalProductsSold(@PathVariable Long businessId){
		return saleService.getAnnualTotalProductsSold(businessId);
	}

	@GetMapping("/{businessId}/productsOverview")
	public ResponseEntity<SaleDTO.ProductsOverview> getProductsOverview(@PathVariable Long businessId){
		return saleService.getProductsOverview(businessId);
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

	@GetMapping("/{businessId}/revenueOverview")
	public ResponseEntity<SaleDTO.RevenueOverview> getRevenueOverview(@PathVariable Long businessId){
		return saleService.getRevenueOverview(businessId);
	}
}
