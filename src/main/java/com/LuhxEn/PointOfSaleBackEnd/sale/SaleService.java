package com.LuhxEn.PointOfSaleBackEnd.sale;

import com.LuhxEn.PointOfSaleBackEnd.batch.Batch;
import com.LuhxEn.PointOfSaleBackEnd.batch.BatchRepository;
import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.InsufficientStockException;
import com.LuhxEn.PointOfSaleBackEnd.exception.ProductNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.product.Product;
import com.LuhxEn.PointOfSaleBackEnd.product.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {
	private final SaleRepository saleRepository;
	private final BusinessRepository businessRepository;
	private final ProductRepository productRepository;
	private final BatchRepository batchRepository;

	@PersistenceContext
	private final EntityManager entityManager;


	@Transactional
	// Should some db query operations fail, the process of creating a sale would fail altogether to ensure ACID compliance
	public ResponseEntity<?> createSale(Long businessId, SaleDTO.SaleRequest saleRequestDTO) {
		List<SaleDTO.CartItem> cartItems = saleRequestDTO.getCartItems();

		if (cartItems.isEmpty()) {
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
		double recomputedGrandTotal = 0;

		double costOfGoodsSold = 0;


		// Set transaction date to the current date and time
		sale.setTransactionDate(new Date());

		// Create an empty arraylist of type ProductListDTO
		List<SaleDTO.ProductList> productListDTOs = new ArrayList<>();



		// Loop through the request body (list of SaleRequestDTO objects)
		for (SaleDTO.CartItem cartItem : cartItems) {
			Long productId = cartItem.getProductId();
			int totalQuantity = cartItem.getQuantity();
			int totalQuantityCopy = totalQuantity;


			// Retrieve product from database
			Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException("Product not found"));

			//TODO: Update stock of the product and check for sufficient stock
			List<Batch> batches = batchRepository.getNonExpiredBatch(productId);
			List<Batch> batchesToUpdate = new ArrayList<>();
			int batchTotalQuantity = batches.stream().mapToInt(Batch::getStock).sum();
			System.out.println("Batch Total Quantity: " + batchTotalQuantity);


			if(totalQuantity <= batchTotalQuantity){
				for(Batch batch : batches){
					if(totalQuantity > 0){
						int quantityToDeduct = Math.min(totalQuantity, batch.getStock());

						batch.setStock(batch.getStock() - quantityToDeduct);

						batchesToUpdate.add(batch);

						totalQuantity -= quantityToDeduct;
					}else{
						break;
					}
				}

				batchRepository.saveAll(batchesToUpdate);
			}else{
				throw new InsufficientStockException("Product does not have enough stocks");
			}


			int newStock = batchTotalQuantity - totalQuantityCopy;

			product.setTotalStock(newStock);

			// Calculate subtotal for the product
			double subtotal = totalQuantityCopy * product.getSellingPrice();

			// Cost of Goods Sold Subtotal
			double cogsSubtotal = totalQuantityCopy * product.getPurchasePrice();

			// Create ProductListDTO to represent the product in the response
			SaleDTO.ProductList productListDTO = SaleDTO.ProductList.builder()
				.productId(product.getId())
				.productName(product.getProductName())
				.quantity(totalQuantityCopy)
				.subtotal(subtotal)
				.build();

			// after creating ProductListDTO object, we add that to the arraylist we defined earlier
			productListDTOs.add(productListDTO);

			// Increment total grand total
			grandTotal += subtotal;

			// Increment total cost of goods sold
			costOfGoodsSold += cogsSubtotal;

			// Create SaleProduct entity to represent the association between sale and product
			SaleProduct saleProduct = new SaleProduct();
			saleProduct.setQuantity(totalQuantityCopy);

			saleProduct.setSubtotal(subtotal);
			// Reference of sale to saleProduct
			sale.getSaleProduct().add(saleProduct);

			// Reference of product to saleProduct;
			saleProduct.setProduct(product);
		}

		sale.setGrandTotal(grandTotal);

		sale.setDiscount(saleRequestDTO.getDiscount());

		recomputedGrandTotal = grandTotal - saleRequestDTO.getDiscount();

		sale.setRecomputedGrandTotal(recomputedGrandTotal);

		double profit = recomputedGrandTotal - costOfGoodsSold;

		if(profit < 0){
			sale.setProfit(0);
		}else{
			sale.setProfit(profit);
		}

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


	public ResponseEntity<SaleResponse> getSalesPaginated(Long businessId, int page, int size){
		businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));
		Pageable pageable = PageRequest.of(page,size);
		Page<Sale> salesPaginated = saleRepository.getSalesPaginated(businessId, pageable);
		List<Sale> sales = salesPaginated.getContent();
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

		SaleResponse saleResponse = SaleResponse
			.builder()
			.content(saleResponseDTOs)
			.pageNo(salesPaginated.getNumber())
			.pageSize(salesPaginated.getSize())
			.totalElements(salesPaginated.getTotalElements())
			.totalPages(salesPaginated.getTotalPages())
			.last(salesPaginated.isLast())
			.build();
		return ResponseEntity.ok(saleResponse);
	}

	// Helper method to convert products to DTOs
	private List<SaleDTO.ProductList> convertProductsToDTOs(Set<SaleProduct> saleProducts) {
		return saleProducts.stream()
			.map(saleProduct -> {
				Product product = saleProduct.getProduct();
				SaleDTO.ProductList productListDTO = new SaleDTO.ProductList();
				productListDTO.setProductId(product.getId());
				productListDTO.setProductName(product.getProductName());
				productListDTO.setQuantity(getQuantity(product.getId(), saleProducts));
				productListDTO.setSubtotal(saleProduct.getSubtotal());
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

	public ResponseEntity<SaleDTO.DailyTotalRevenue> getDailyTotalRevenue(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		LocalDate today = LocalDate.now();

		// Call the repository method to get total sale amount
		double overall = saleRepository.getDailyTotalRevenue(selectedBusiness.getId(), today);

		// Construct the response
		SaleDTO.DailyTotalRevenue dailyTotalRevenue = SaleDTO.DailyTotalRevenue
			.builder()
			.dailyTotalRevenue(overall)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(dailyTotalRevenue);
	}

	public ResponseEntity<SaleDTO.MonthlyTotalRevenue> getMonthlyTotalRevenue(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		double overall = saleRepository.getMonthlyTotalRevenue(selectedBusiness.getId());

		SaleDTO.MonthlyTotalRevenue monthlyTotalRevenue = SaleDTO.MonthlyTotalRevenue
			.builder()
			.monthlyTotalRevenue(overall)
			.build();


		return ResponseEntity.status(HttpStatus.OK).body(monthlyTotalRevenue);
	}

	public ResponseEntity<SaleDTO.AnnualTotalRevenue> getAnnualTotalRevenue(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		double overall = saleRepository.getAnnualTotalRevenue(selectedBusiness.getId());

		SaleDTO.AnnualTotalRevenue annualTotalRevenue = SaleDTO.AnnualTotalRevenue
			.builder()
			.annualTotalRevenue(overall)
			.build();


		return ResponseEntity.status(HttpStatus.OK).body(annualTotalRevenue);
	}

	public ResponseEntity<List<SaleDTO.MonthlyRevenueAndProfits>> getMonthlyRevenuesForTheYear(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		List<SaleDTO.MonthlyRevenueAndProfits> monthlyRevenues = new ArrayList<>();


		LocalDate currentDate = LocalDate.now();
		int currentYear = currentDate.getYear();

		for (int month = 1; month <= 12; month++) {
			double totalRevenue = saleRepository.getMonthlyRevenuesForTheYear(selectedBusiness.getId(), currentYear, month);
			double totalProfit = saleRepository.getMonthlyProfitsForTheYear(selectedBusiness.getId(), currentYear,month);

			SaleDTO.MonthlyRevenueAndProfits revenues = SaleDTO.MonthlyRevenueAndProfits
				.builder()
				.year(currentYear)
				.month(month)
				.monthlyRevenuesForTheYear(totalRevenue)
				.monthlyProfitsForTheYear(totalProfit)
				.build();
			monthlyRevenues.add(revenues);
		}

		return ResponseEntity.status(HttpStatus.OK).body(monthlyRevenues);
	}

	public ResponseEntity<SaleDTO.DailyProfit> getDailyProfit(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		LocalDate today = LocalDate.now();


		double overall = saleRepository.getDailyProfit(selectedBusiness.getId(), today);

		SaleDTO.DailyProfit dailyProfit = SaleDTO.DailyProfit
			.builder()
			.dailyProfit(overall)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(dailyProfit);
	}

	public ResponseEntity<SaleDTO.MonthlyProfit> getMonthlyProfit(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));
		double profitValue = saleRepository.getMonthlyProfit(selectedBusiness.getId());
		SaleDTO.MonthlyProfit monthlyProfit = SaleDTO.MonthlyProfit
			.builder()
			.monthlyProfit(profitValue)
			.build();
		return ResponseEntity.status(HttpStatus.OK).body(monthlyProfit);
	}

	public ResponseEntity<SaleDTO.AnnualProfit> getAnnualProfit(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));
		double profitValue = saleRepository.getAnnualProfit(selectedBusiness.getId());
		SaleDTO.AnnualProfit annualProfit = SaleDTO.AnnualProfit
			.builder()
			.annualProfit(profitValue)
			.build();
		return ResponseEntity.status(HttpStatus.OK).body(annualProfit);
	}

	public ResponseEntity<SaleDTO.RevenueOverview> getRevenueOverview(Long businessId){
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));
		LocalDate today = LocalDate.now();

		double dailyTotalRevenue = saleRepository.getDailyTotalRevenue(selectedBusiness.getId(), today);
		double monthlyTotalRevenue = saleRepository.getMonthlyTotalRevenue(selectedBusiness.getId());
		double annualTotalRevenue = saleRepository.getAnnualTotalRevenue(selectedBusiness.getId());
		double dailyProfit = saleRepository.getDailyProfit(selectedBusiness.getId(), today);
		double monthlyProfit = saleRepository.getMonthlyProfit(selectedBusiness.getId());
		double annualProfit = saleRepository.getAnnualProfit(selectedBusiness.getId());

		SaleDTO.RevenueOverview revenueOverview = SaleDTO.RevenueOverview
			.builder()
			.dailyTotalRevenue(dailyTotalRevenue)
			.monthlyTotalRevenue(monthlyTotalRevenue)
			.annualTotalRevenue(annualTotalRevenue)
			.dailyProfit(dailyProfit)
			.monthlyProfit(monthlyProfit)
			.annualProfit(annualProfit)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(revenueOverview);
	}


	public ResponseEntity<SaleDTO.DailyTotalProductsSold> getDailyTotalProductsSold(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		LocalDate today = LocalDate.now();

		int totalProductsSold = saleRepository.getDailyTotalProductsSold(selectedBusiness.getId(), today);

		SaleDTO.DailyTotalProductsSold dailyTotalProductsSold = SaleDTO.DailyTotalProductsSold
			.builder()
			.dailyTotalProductsSold(totalProductsSold)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(dailyTotalProductsSold);
	}


	public ResponseEntity<SaleDTO.MonthlyTotalProductsSold> getMonthlyTotalProductsSold(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		int totalProductsSold = saleRepository.getMonthlyTotalProductsSold(selectedBusiness.getId());
		SaleDTO.MonthlyTotalProductsSold monthlyTotalProductsSold = SaleDTO.MonthlyTotalProductsSold
			.builder()
			.monthlyTotalProductsSold(totalProductsSold)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(monthlyTotalProductsSold);
	}

	public ResponseEntity<SaleDTO.AnnualTotalProductsSold> getAnnualTotalProductsSold(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		int totalProductsSold = saleRepository.getAnnualTotalProductsSold(selectedBusiness.getId());
		SaleDTO.AnnualTotalProductsSold annualTotalProductsSold = SaleDTO.AnnualTotalProductsSold
			.builder()
			.annualTotalProductsSold(totalProductsSold)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(annualTotalProductsSold);
	}

	public ResponseEntity<SaleDTO.ProductsOverview> getProductsOverview(Long businessId){
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));
		LocalDate today = LocalDate.now();

		int dailyTotalProductsSold = saleRepository.getDailyTotalProductsSold(selectedBusiness.getId(), today);
		int monthlyTotalProductsSold = saleRepository.getMonthlyTotalProductsSold(selectedBusiness.getId());
		int annualTotalProductsSold = saleRepository.getAnnualTotalProductsSold(selectedBusiness.getId());
		int totalStocks = batchRepository.getTotalStocks(selectedBusiness.getId());

		SaleDTO.ProductsOverview productsOverview = SaleDTO.ProductsOverview
			.builder()
			.dailyTotalProductsSold(dailyTotalProductsSold)
			.monthlyTotalProductsSold(monthlyTotalProductsSold)
			.annualTotalProductsSold(annualTotalProductsSold)
			.totalStocks(totalStocks)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(productsOverview);
	}



	public ResponseEntity<List<SaleDTO.MonthlyTotalSoldForTheYear>> getMonthlySoldForTheYear(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		// Initialize a list to hold yearly totals
		List<SaleDTO.MonthlyTotalSoldForTheYear> yearlyTotals = new ArrayList<>();

		// Get the current year
		LocalDate currentDate = LocalDate.now();
		int currentYear = currentDate.getYear();

		// Retrieve total products sold for each month of the current year
		for (int month = 1; month <= 12; month++) {
			// Retrieve total products sold for the current month
			int totalProductsSold = saleRepository.getMonthlySoldForTheYear(selectedBusiness.getId(), currentYear, month);

			// Create YearlyTotalProductsSold object and add it to the list
			SaleDTO.MonthlyTotalSoldForTheYear yearlyTotalProductsSold = SaleDTO.MonthlyTotalSoldForTheYear
				.builder()
				.year(currentYear)
				.month(month)
				.monthlyTotalProductsSold(totalProductsSold)
				.build();
			yearlyTotals.add(yearlyTotalProductsSold);
		}

		return ResponseEntity.status(HttpStatus.OK).body(yearlyTotals);
	}


	public ResponseEntity<SaleDTO.Dashboard> getDashboardValues(Long businessId) {
		Business selectedBusiness = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		LocalDate today = LocalDate.now();

		double dailyTotalRevenue = saleRepository.getDailyTotalRevenue(selectedBusiness.getId(), today);
		double monthlyTotalRevenue = saleRepository.getMonthlyTotalRevenue(selectedBusiness.getId());
		int dailyTotalProductsSold = saleRepository.getDailyTotalProductsSold(selectedBusiness.getId(), today);
		int monthlyTotalProductsSold = saleRepository.getMonthlyTotalProductsSold(selectedBusiness.getId());

		SaleDTO.Dashboard dashboard = SaleDTO.Dashboard
			.builder()
			.dailyTotalRevenue(dailyTotalRevenue)
			.monthlyTotalRevenue(monthlyTotalRevenue)
			.dailyTotalProductsSold(dailyTotalProductsSold)
			.monthlyTotalProductSold(monthlyTotalProductsSold)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(dashboard);
	}


	//TODO: BUGFIX
//	public ResponseEntity<List<SaleDTO.PopularProductDTO>> getMostPopularProducts(Long businessId) {
//		businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));
//
//		List<Object[]> popularProducts = saleRepository.getMostPopularProducts(businessId);
//		System.out.println("POPULAR PRODUCTS: " + popularProducts);
//
//		List<SaleDTO.PopularProductDTO> popularProductDTOS = new ArrayList<>();
//		for (Object[] row : popularProducts) {
//			String productName = (String) row[9];
//			Long quantitySold = (Long) row[10];
//			popularProductDTOS.add(new SaleDTO.PopularProductDTO(productName, quantitySold));
//		}
//
//		return ResponseEntity.status(HttpStatus.OK).body(popularProductDTOS);
//	}

	public ResponseEntity<List<SaleDTO.PopularProductDTO>> getMostPopularProducts(Long businessId) {
		businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		List<Object[]> popularProducts = saleRepository.getMostPopularProducts(businessId);

		List<SaleDTO.PopularProductDTO> popularProductDTOS = new ArrayList<>();
		for (Object[] row : popularProducts) {
			String productName = (String) row[8];
			Long quantitySold = (Long) row[9];
			popularProductDTOS.add(new SaleDTO.PopularProductDTO(productName, quantitySold));
		}

		return ResponseEntity.status(HttpStatus.OK).body(popularProductDTOS);
	}

}

