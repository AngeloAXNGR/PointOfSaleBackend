package com.LuhxEn.PointOfSaleBackEnd.product;


import com.LuhxEn.PointOfSaleBackEnd.batch.Batch;
import com.LuhxEn.PointOfSaleBackEnd.batch.BatchRepository;
import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.category.Category;
import com.LuhxEn.PointOfSaleBackEnd.category.CategoryRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.CategoryNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.ProductNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.sale.SaleDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
	private final BusinessRepository businessRepository;
	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;
	private final BatchRepository batchRepository;

	private final EntityManager entityManager;


//	public ResponseEntity<List<Product>> getProducts(Long businessId){
//		Business selectedBusiness = businessRepository.getReferenceById(businessId);
//		List<Product> products = new ArrayList<>(selectedBusiness.getProducts());
//
//		// Sort Alphabetically by productName
//		Collections.sort(products, Comparator.comparing(Product::getProductName));
//		return ResponseEntity.status(HttpStatus.OK).body(products);
//	}

	public ResponseEntity<List<Product>> getProducts(Long businessId) {
		List<Product> products = productRepository.getProductsAsc(businessId);
		return ResponseEntity.status(HttpStatus.OK).body(products);
	}

	public ResponseEntity<?> addProducts(Long businessId, List<ProductDTO.ProductRequest> productDTOs) {
		// Business Reference
		Business selectedBusiness = businessRepository.findById(businessId)
			.orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		List<Product> productList = new ArrayList<>();
		List<Batch> batches = new ArrayList<>();

		for (ProductDTO.ProductRequest productDTO : productDTOs) {
			// Get Category ID to be used by the product object
			Category category = categoryRepository.findById(productDTO.getCategoryId())
				.orElseThrow(() -> new CategoryNotFoundException("Category Not Found"));

			Product existingProduct = productRepository.findByProductNameIgnoreCase(productDTO.getProductName());
			if (existingProduct != null) {
				// If Product Exists, we create a batch and provide reference to a product
				// No need to create a new product, just the batch
				Optional<Batch> existingBatch = batchRepository.getBatch(existingProduct.getId()).stream().filter(batch -> {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					String batchDateStr = dateFormat.format(batch.getExpirationDate());
					String productDateStr = dateFormat.format(productDTO.getExpirationDate());

					return batchDateStr.equals(productDateStr);
				}).findFirst();

				if (existingBatch.isPresent()) {
					System.out.println("BREAK POINT 3");
					Batch batchToUpdate = existingBatch.get();
					batchToUpdate.setStock(batchToUpdate.getStock() + productDTO.getStock());
					batches.add(batchToUpdate);
				} else {
					Batch batch = Batch
						.builder()
						.expirationDate(productDTO.getExpirationDate())
						.stock(productDTO.getStock())
						.product(existingProduct)
						.build();
					batches.add(batch);
				}
				existingProduct.setTotalStock(existingProduct.getTotalStock() + productDTO.getStock());
				productList.add(existingProduct);
			} else {
				// Create both the product and the batch
				Product newProduct = Product
					.builder()
					.productName(productDTO.getProductName())
					.purchasePrice(productDTO.getPurchasePrice())
					.sellingPrice(productDTO.getSellingPrice())
					.totalStock(productDTO.getStock())
					.lowStockThreshold(productDTO.getLowStockThreshold())
					.isDeleted(productDTO.isDeleted())
					.build();

				Batch batch = Batch
					.builder()
					.expirationDate(productDTO.getExpirationDate())
					.stock(productDTO.getStock())
					.product(newProduct)
					.build();

				batches.add(batch);
				selectedBusiness.getProducts().add(newProduct);
				category.getProducts().add(newProduct);
				productList.add(newProduct);
			}


		}

		productRepository.saveAll(productList);
		batchRepository.saveAll(batches);

		return ResponseEntity.status(HttpStatus.OK).body(productList);

	}

	
//	public ResponseEntity<Product> updateProduct(Long id, Product product){
//		return productRepository.findById(id).map(product1 -> {
//			product1.setProductName(product.getProductName());
//			product1.setCategoryId(product.getCategoryId());
//			product1.setPurchasePrice(product.getPurchasePrice());
//			product1.setSellingPrice(product.getSellingPrice());
//			product1.setStock(product.getStock());
//			product1.setLowStockThreshold(product.getLowStockThreshold());
//			product1.setExpiration(product.getExpiration());
//			product1.setDaysBeforeExpiration(product.getDaysBeforeExpiration());
//			Product updatedProduct = productRepository.save(product1);
//			return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
//		}).orElseGet(() -> ResponseEntity.notFound().build());
//	}

	public ResponseEntity<String> deleteProduct(Long id) {
		// HARD DELETE
//		Optional<Product> productOptional = productRepository.findById(id);
//		if(productOptional.isEmpty()){
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with the id of " + id + " was not found.");
//		}
//		productRepository.deleteById(id);
//		return ResponseEntity.status(HttpStatus.OK).body("Product with the id of " + id + " was deleted.");

		// SOFT DELETE
		return productRepository.findById(id).map(product -> {
			product.setDeleted(true);
			productRepository.save(product);
			return ResponseEntity.status(HttpStatus.OK).body("Product with id of " + id + " was deleted.");
		}).orElseGet(() -> ResponseEntity.notFound().build());

	}
}
