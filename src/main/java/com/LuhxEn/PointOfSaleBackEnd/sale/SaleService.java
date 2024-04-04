package com.LuhxEn.PointOfSaleBackEnd.sale;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.InsufficientStockException;
import com.LuhxEn.PointOfSaleBackEnd.exception.ProductNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.product.Product;
import com.LuhxEn.PointOfSaleBackEnd.product.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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


	@Transactional // Should some db query operations fail, the process of creating a sale would fail altogether to ensure ACID compliance
	public ResponseEntity<?> createSale(Long businessId, List<SaleRequestDTO> saleRequestDTOs) {
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
		List<ProductListDTO> productListDTOs = new ArrayList<>();

		// Loop through the request body (list of SaleRequestDTO objects)
		for (SaleRequestDTO saleRequestDTO : saleRequestDTOs) {

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
			ProductListDTO productListDTO = ProductListDTO.builder()
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

		// Save the sale entity along with associated sale products
		sale = saleRepository.save(sale);

		// Add the sale reference to the business
		business.getSales().add(sale);

		// Update the business in the database
		businessRepository.save(business);

		// Create and return the SaleResponseDTO
		SaleResponseDTO saleResponseDTO = SaleResponseDTO.builder()
			.saleId(sale.getId())
			.products(productListDTOs)
			.transactionDate(sale.getTransactionDate())
			.grandTotal(grandTotal)
			.build();

		return ResponseEntity.ok(saleResponseDTO);
	}



	public ResponseEntity<List<SaleResponseDTO>> getAllSales(Long businessId) {
		Business selectedBusiness = businessRepository.getReferenceById(businessId);
		List<Sale> sales = new ArrayList<>(selectedBusiness.getSales());

		List<SaleResponseDTO> saleResponseDTOs = sales.stream()
			.map(sale -> {
				SaleResponseDTO saleResponseDTO = new SaleResponseDTO();
				saleResponseDTO.setSaleId(sale.getId());
				saleResponseDTO.setProducts(convertProductsToDTOs(sale.getSaleProduct()));
				saleResponseDTO.setTransactionDate(sale.getTransactionDate());
				saleResponseDTO.setGrandTotal(calculateTotal(sale.getSaleProduct()));
				return saleResponseDTO;
			})
			.collect(Collectors.toList());

		return ResponseEntity.ok(saleResponseDTOs);
	}

	// Helper method to convert products to DTOs
	private List<ProductListDTO> convertProductsToDTOs(Set<SaleProduct> saleProducts) {
		return saleProducts.stream()
			.map(saleProduct -> {
				Product product = saleProduct.getProduct();
				ProductListDTO productListDTO = new ProductListDTO();
				productListDTO.setProductId(product.getId());
				productListDTO.setProductName(product.getProductName());
				productListDTO.setQuantity(getQuantity(product.getId(), saleProducts)); // Assuming quantity is always 1 for fetching sales
				productListDTO.setSubtotal(product.getSellingPrice() * productListDTO.getQuantity()); // Assuming no quantity-based calculation here
				return productListDTO;
			})
			.collect(Collectors.toList());
	}

	// Helper method to calculate total sale amount
	private double calculateTotal(Set<SaleProduct> saleProducts) {
		return saleProducts.stream()
			.mapToDouble(saleProduct -> {
				Product product = saleProduct.getProduct(); // Retrieve the product from the SaleProduct
				return product.getSellingPrice() * saleProduct.getQuantity(); // Calculate subtotal for the saleProduct
			})
			.sum();
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


}

