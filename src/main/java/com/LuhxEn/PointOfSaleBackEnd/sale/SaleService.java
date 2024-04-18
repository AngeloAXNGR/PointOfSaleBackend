package com.LuhxEn.PointOfSaleBackEnd.sale;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.InsufficientStockException;
import com.LuhxEn.PointOfSaleBackEnd.exception.ProductNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.product.Product;
import com.LuhxEn.PointOfSaleBackEnd.product.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {
	private final SaleRepository saleRepository;
	private final BusinessRepository businessRepository;
	private final ProductRepository productRepository;

	@PersistenceContext
	private final EntityManager entityManager;


	@Transactional // Should some db query operations fail, the process of creating a sale would fail altogether to ensure ACID compliance
	public ResponseEntity<?> createSale(Long businessId, List<SaleDTO.SaleRequest> saleRequestDTOs) {
		if (saleRequestDTOs.isEmpty()) {
			// Return a response indicating that the request is invalid
			return ResponseEntity.badRequest().body("Bad Request");
		}

		// Creating a reference of the business, used to associate the sale data later on
		Business business = businessRepository.findById(businessId)
			.orElseThrow(() -> new BusinessNotFoundException("Business not found"));

		// Instantiate Sale object
		Sale sale = new Sale();

		// Initial Sale GrandTotal (To be recomputed later)
		double grandTotal = 0;

		// Set transaction date to the current date and time
		sale.setTransactionDate(new Date());

		// Create an empty arraylist of type ProductListDTO
		List<SaleDTO.ProductList> productListDTOs = new ArrayList<>();

		// Loop through the request body (list of SaleRequestDTO objects)
		for (SaleDTO.SaleRequest saleRequestDTO : saleRequestDTOs) {

			Long productId = saleRequestDTO.getProductId();
			int quantity = saleRequestDTO.getQuantity();

			// Retrieve product from database
			Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException("Product not found"));

			// Update stock of the product and check for sufficient stock
			int newStock = product.getStock() - quantity;
			if (newStock < 0) {
				throw new InsufficientStockException("Product does not have enough stock");
			}

			product.setStock(newStock);

			// Calculate subtotal for the product
			double subtotal = quantity * product.getSellingPrice();

			// Create ProductListDTO to represent the product in the response
			SaleDTO.ProductList productListDTO = SaleDTO.ProductList.builder()
				.productId(product.getId())
				.productName(product.getProductName())
				.quantity(quantity)
				.subtotal(subtotal)
				.build();

			// after creating ProductListDTO object, we add that to the arraylist we defined earlier
			productListDTOs.add(productListDTO);

			// Increment total grand total
			grandTotal += subtotal;

			// Create SaleProduct entity to represent the association between sale and product
			SaleProduct saleProduct = new SaleProduct();
			saleProduct.setQuantity(quantity);

			// Reference of sale to saleProduct
			sale.getSaleProduct().add(saleProduct);

			// Reference of product to saleProduct;
			saleProduct.setProduct(product);
		}

		sale.setGrandTotal(grandTotal);

		// Save the sale entity along with associated sale products
		sale = saleRepository.save(sale);

		// Add the sale reference to the business
		business.getSales().add(sale);

		// Update the business in the database
		businessRepository.save(business);

		// Create and return the SaleResponseDTO
		SaleDTO.SaleResponse saleResponseDTO = SaleDTO.SaleResponse.builder()
			.saleId(sale.getId())
			.products(productListDTOs)
			.transactionDate(sale.getTransactionDate())
			.grandTotal(sale.getGrandTotal())
			.build();

		return ResponseEntity.ok(saleResponseDTO);
	}


	public ResponseEntity<List<SaleDTO.SaleResponse>> getAllSales(Long businessId) {
		try {
			Business selectedBusiness = businessRepository.getReferenceById(businessId);

			List<Sale> sales = new ArrayList<>(selectedBusiness.getSales());

			List<SaleDTO.SaleResponse> saleResponseDTOs = sales.stream()
				.map(sale -> {
					SaleDTO.SaleResponse saleResponseDTO = new SaleDTO.SaleResponse();
					saleResponseDTO.setSaleId(sale.getId());
					saleResponseDTO.setProducts(convertProductsToDTOs(sale.getSaleProduct()));
					saleResponseDTO.setTransactionDate(sale.getTransactionDate());
					saleResponseDTO.setGrandTotal(sale.getGrandTotal());
					return saleResponseDTO;
				})
				.collect(Collectors.toList());

			return ResponseEntity.ok(saleResponseDTOs);
		} catch (Exception e) {
			throw new BusinessNotFoundException("Business Not Found.");
		}

	}

	// Helper method to convert products to DTOs
	private List<SaleDTO.ProductList> convertProductsToDTOs(Set<SaleProduct> saleProducts) {
		return saleProducts.stream()
			.map(saleProduct -> {
				Product product = saleProduct.getProduct();
				SaleDTO.ProductList productListDTO = new SaleDTO.ProductList();
				productListDTO.setProductId(product.getId());
				productListDTO.setProductName(product.getProductName());
				productListDTO.setQuantity(getQuantity(product.getId(), saleProducts)); // Assuming quantity is always 1 for fetching sales
				productListDTO.setSubtotal(product.getSellingPrice() * productListDTO.getQuantity()); // Assuming no quantity-based calculation here
				return productListDTO;
			})
			.collect(Collectors.toList());
	}


	// Helper method to get the quantity of a product sold in a sale
	private int getQuantity(Long productId, Set<SaleProduct> saleProducts) {
		int totalQuantity = 0;

		for (SaleProduct saleProduct : saleProducts) {
			if (saleProduct.getProduct().getId().equals(productId)) {
				totalQuantity += saleProduct.getQuantity();
			}
		}

		return totalQuantity;
	}


//	public ResponseEntity<List<SaleDTO.SaleResponse>> getSalesForToday(Long businessId) {
//		Business selectedBusiness = businessRepository.getReferenceById(businessId);
//		List<Sale> sales = new ArrayList<>(selectedBusiness.getSales());
//
//		LocalDate today = LocalDate.now();
//		List<Sale> salesForToday = sales.stream()
//			.filter(sale -> {
//				LocalDate transactionDate = sale.getTransactionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//				return transactionDate.equals(today);
//			})
//			.collect(Collectors.toList());
//
//		List<SaleDTO.SaleResponse> saleResponseDTOs = salesForToday.stream()
//			.map(sale -> {
//				SaleDTO.SaleResponse saleResponseDTO = new SaleDTO.SaleResponse();
//				saleResponseDTO.setSaleId(sale.getId());
//				saleResponseDTO.setProducts(convertProductsToDTOs(sale.getSaleProduct()));
//				saleResponseDTO.setTransactionDate(sale.getTransactionDate());
//				saleResponseDTO.setGrandTotal(sale.getGrandTotal());
//				return saleResponseDTO;
//			})
//			.collect(Collectors.toList());
//
//		return ResponseEntity.ok(saleResponseDTOs);
//	}

//	public ResponseEntity<SaleDTO.TodayTotalSaleAmount> getTodayTotalSaleAmount(Long businessId) {
//		Business selectedBusiness = businessRepository.getReferenceById(businessId);
//		List<Sale> sales = new ArrayList<>(selectedBusiness.getSales());
//
//		LocalDate today = LocalDate.now();
//		List<Sale> salesForToday = sales.stream()
//			.filter(sale -> {
//				LocalDate transactionDate = sale.getTransactionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
//				return transactionDate.equals(today);
//			})
//			.collect(Collectors.toList());
//
//		double overall = 0;
//		for(Sale sale : salesForToday){
//			overall += sale.getGrandTotal();
//		}
//
//		SaleDTO.TodayTotalSaleAmount todayOverallSale = new SaleDTO.TodayTotalSaleAmount();
//		todayOverallSale.setTotalSaleAmount(overall);
//
//		return ResponseEntity.status(HttpStatus.OK).body(todayOverallSale);
//	}

	public ResponseEntity<SaleDTO.DailyTotalSaleAmount> getDailyTotalSaleAmount(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		LocalDate today = LocalDate.now();

		// Call the repository method to get total sale amount
		double overall = saleRepository.getTotalSaleAmountForToday(selectedBusiness.getId(), today);

		// Construct the response
		SaleDTO.DailyTotalSaleAmount todayOverallSale = new SaleDTO.DailyTotalSaleAmount();
		todayOverallSale.setDailyTotalSaleAmount(overall);

		return ResponseEntity.status(HttpStatus.OK).body(todayOverallSale);
	}

	public ResponseEntity<SaleDTO.MonthlyTotalSaleAmount> getMonthlyTotalSaleAmount(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		double overall = saleRepository.getTotalSaleAmountForTheMonth(selectedBusiness.getId());

		SaleDTO.MonthlyTotalSaleAmount monthlyOverallSale = new SaleDTO.MonthlyTotalSaleAmount();
		monthlyOverallSale.setMonthlyTotalSaleAmount(overall);

		return ResponseEntity.status(HttpStatus.OK).body(monthlyOverallSale);
	}

	public ResponseEntity<List<SaleDTO.MonthlySaleForTheYear>> getMonthlySaleForYear(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		List<SaleDTO.MonthlySaleForTheYear> yearlySales = new ArrayList<>();

		LocalDate currentDate = LocalDate.now();
		int currentYear = currentDate.getYear();

		for (int month = 1; month <= 12; month++) {
			double totalSaleAmount = saleRepository.getTotalSaleAmountForTheYear(selectedBusiness.getId(), currentYear, month);

			SaleDTO.MonthlySaleForTheYear yearlyTotalSaleAmount = new SaleDTO.MonthlySaleForTheYear();
			yearlyTotalSaleAmount.setYear(currentYear);
			yearlyTotalSaleAmount.setMonth(month);
			yearlyTotalSaleAmount.setMonthlyTotalSaleAmount(totalSaleAmount);
			yearlySales.add(yearlyTotalSaleAmount);
		}

		return ResponseEntity.status(HttpStatus.OK).body(yearlySales);
	}

	public ResponseEntity<SaleDTO.DailyTotalProductsSold> getDailyTotalProductsSold(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		LocalDate today = LocalDate.now();

		int totalProductsSold = saleRepository.getTotalProductsSoldForToday(selectedBusiness.getId(), today);

		SaleDTO.DailyTotalProductsSold dailyTotalProductsSold = new SaleDTO.DailyTotalProductsSold();
		dailyTotalProductsSold.setDailyTotalProductsSold(totalProductsSold);

		return ResponseEntity.status(HttpStatus.OK).body(dailyTotalProductsSold);
	}


	public ResponseEntity<SaleDTO.MonthlyTotalProductsSold> getMonthlyTotalProductsSold(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		int totalProductsSold = saleRepository.getTotalProductsSoldForTheMonth(selectedBusiness.getId());
		SaleDTO.MonthlyTotalProductsSold monthlyTotalProductsSold = new SaleDTO.MonthlyTotalProductsSold();
		monthlyTotalProductsSold.setMonthlyTotalProductsSold(totalProductsSold);

		return ResponseEntity.status(HttpStatus.OK).body(monthlyTotalProductsSold);
	}

	public ResponseEntity<List<SaleDTO.MonthlyTotalSoldForTheYear>> getMonthlyTotalSoldForYear(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		// Initialize a list to hold yearly totals
		List<SaleDTO.MonthlyTotalSoldForTheYear> yearlyTotals = new ArrayList<>();

		// Get the current year
		LocalDate currentDate = LocalDate.now();
		int currentYear = currentDate.getYear();

		// Retrieve total products sold for each month of the current year
		for (int month = 1; month <= 12; month++) {
			// Retrieve total products sold for the current month
			int totalProductsSold = saleRepository.getTotalProductsSoldForTheYear(selectedBusiness.getId(), currentYear, month);

			// Create YearlyTotalProductsSold object and add it to the list
			SaleDTO.MonthlyTotalSoldForTheYear yearlyTotalProductsSold = new SaleDTO.MonthlyTotalSoldForTheYear();
			yearlyTotalProductsSold.setYear(currentYear);
			yearlyTotalProductsSold.setMonth(month);
			yearlyTotalProductsSold.setMonthlyTotalProductsSold(totalProductsSold);
			yearlyTotals.add(yearlyTotalProductsSold);
		}

		return ResponseEntity.status(HttpStatus.OK).body(yearlyTotals);
	}


	public ResponseEntity<SaleDTO.Dashboard> getDashboardValues(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		LocalDate today = LocalDate.now();

		double dailyTotalSaleAmount = saleRepository.getTotalSaleAmountForToday(selectedBusiness.getId(), today);
		double monthlyTotalSaleAmount = saleRepository.getTotalSaleAmountForTheMonth(selectedBusiness.getId());
		int dailyTotalProductsSold = saleRepository.getTotalProductsSoldForToday(selectedBusiness.getId(), today);
		int monthlyTotalProductsSold = saleRepository.getTotalProductsSoldForTheMonth(selectedBusiness.getId());

		SaleDTO.Dashboard dashboard = SaleDTO.Dashboard
			.builder()
			.dailyTotalSaleAmount(dailyTotalSaleAmount)
			.monthlyTotalSaleAmount(monthlyTotalSaleAmount)
			.dailyTotalProductsSold(dailyTotalProductsSold)
			.monthlyTotalProductSold(monthlyTotalProductsSold)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(dashboard);
	}


	public ResponseEntity<List<SaleDTO.PopularProductDTO>> getMostPopularProducts(Long businessId) {
		businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		List<Object[]> popularProducts = saleRepository.getMostPopularProducts(businessId);

		List<SaleDTO.PopularProductDTO> popularProductDTOS = new ArrayList<>();
		for (Object[] row : popularProducts) {
			String productName = (String) row[10];
			Long quantitySold = (Long) row[11];
			popularProductDTOS.add(new SaleDTO.PopularProductDTO(productName, quantitySold));
		}

		return ResponseEntity.status(HttpStatus.OK).body(popularProductDTOS);


	}


}

