package com.LuhxEn.PointOfSaleBackEnd.sale;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.product.Product;
import com.LuhxEn.PointOfSaleBackEnd.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {
	private final SaleRepository saleRepository;
	private final BusinessRepository businessRepository;
	private final ProductRepository productRepository;


	public ResponseEntity<SaleResponseDTO> createSale(Long businessId, List<SaleRequestDTO> saleRequestDTOs){
		Business business = businessRepository.findById(businessId).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		// instantiate Sale object
		Sale sale = new Sale();
		double grandTotal = 0;

		List<ProductListDTO> productListDTOs = new ArrayList<>();

		// loop through the request body (which is an array SaleRequestDTO objects)
		for(SaleRequestDTO saleRequestDTO: saleRequestDTOs){
			Long productId = saleRequestDTO.getProductId();
			int quantity = saleRequestDTO.getQuantity();

			// TODO: Replace RuntimeException
			Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product Not Found"));

			// TODO: Add quantity validation
			// update stock of that product (Reduce stocks based on specified quantity
			int newStock = product.getStock() - quantity;
			product.setStock(newStock);

			// Save the updated product
			productRepository.save(product);

			double subtotal = quantity * product.getSellingPrice();
			ProductListDTO productListDTO = ProductListDTO
				.builder()
				.productId(product.getId())
				.productName(product.getProductName())
				.quantity(quantity)
				.subtotal(subtotal)
				.build();

			productListDTOs.add(productListDTO);

			// Increment total
			grandTotal += subtotal;

			// Add the product to the sale's products
			sale.getProducts().add(product);
		}

		// Save the sale entity
		sale = saleRepository.save(sale);

		business.getSales().add(sale);

		businessRepository.save(business);

		SaleResponseDTO saleResponseDTO = SaleResponseDTO
			.builder()
			.saleId(sale.getId())
			.products(productListDTOs)
			.grandTotal(grandTotal)
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(saleResponseDTO);
	}
}
