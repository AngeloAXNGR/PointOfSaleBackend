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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Field;
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
		List<Product> products = new ArrayList<>();
		List<Product> products1 = productRepository.getProductsAsc(businessId);

		// NEEDS REVISIONING (POSSIBLY INEFFICIENT)
		// MANUALLY UPDATING TOTAL STOCK DUE TO THE POSSIBILITY OF BATCHES BEING EXPIRED
		for (Product product : products1) {
			int totalStock = batchRepository.getTotalStock(product.getId());
			product.setTotalStock(totalStock);
			products.add(product);
		}

		productRepository.saveAll(products);
		return ResponseEntity.status(HttpStatus.OK).body(products);
	}

	public ResponseEntity<ProductResponse> getProductsPaginated(Long businessId, String keyword, int page, int size) {
		// if keyword query value is empty, its default value is 0
		// As a result, optional keyword query is not going to work as intended
		if (keyword.equals("0")) {
			keyword = "";
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<Product> products = productRepository.getProductsPaginated(businessId, keyword, pageable);
		List<Product> products1 = products.getContent();
		List<Product> products2 = new ArrayList<>();
		HashMap<Integer, Integer> productsTotalStock = new HashMap<>();
		List<Long> productIds = new ArrayList<>();


		for(Product product : products1){
			productIds.add(product.getId());
		}

		List<Object[]> totalStocks = batchRepository.getTotalStockByProductIds(productIds);

		for(Object[] totalStock : totalStocks){
			Long productId = (Long) totalStock[0];
			Long total = (Long) totalStock[1];
			productsTotalStock.put(productId.intValue(), total.intValue());
		}

		for (Product product : products1) {
			int totalStock = productsTotalStock.getOrDefault(product.getId().intValue(),0);
			product.setTotalStock(totalStock);
			products2.add(product);
		}

		productRepository.saveAll(products2);

		ProductResponse productResponse = ProductResponse
			.builder()
			.content(products2)
			.pageNo(products.getNumber())
			.pageSize(products.getSize())
			.totalElements(products.getTotalElements())
			.totalPages(products.getTotalPages())
			.last(products.isLast())
			.build();

		return ResponseEntity.status(HttpStatus.OK).body(productResponse);
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

			Product existingProduct = productRepository.findByProductNameIgnoreCase(productDTO.getProductName(), selectedBusiness.getId());
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
						.batchPurchasePrice(productDTO.getPurchasePrice())
						.business(selectedBusiness)
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
					.categoryId(category.getId())
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
					.batchPurchasePrice(productDTO.getPurchasePrice())
					.business(selectedBusiness)
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


	public ResponseEntity<Product> updateProduct(Long id, Product product) {
		return productRepository.findById(id).map(product1 -> {
			Field[] fields = Product.class.getDeclaredFields();
			for (Field field : fields) {
				try {
					// Set accessible to true to access private fields
					field.setAccessible(true);
					// Get the value of the field from the provided product object
					Object value = field.get(product);
					// Update the corresponding field in the existing product object
					if (value != null && !"totalStock".equals(field.getName())) {
						field.set(product1, value);
					}
				} catch (IllegalAccessException e) {
					// Handle any exceptions
					e.printStackTrace();
				}
			}
			Product updatedProduct = productRepository.save(product1);
			return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

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
			product.setTotalStock(0);
			product.setDeleted(true);
			List<Batch> batches = batchRepository.getBatch(id);
			batches.forEach(batch -> {
				batchRepository.delete(batch);
			});
			productRepository.save(product);
			return ResponseEntity.status(HttpStatus.OK).body("Product with id of " + id + " was deleted.");
		}).orElseGet(() -> ResponseEntity.notFound().build());

	}
}
