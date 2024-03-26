package com.LuhxEn.PointOfSaleBackEnd.product;


import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.category.CategoryRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
	private final BusinessRepository businessRepository;
	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;


	public ResponseEntity<List<Product>> getProducts(Long businessId){
		Business selectedBusiness = businessRepository.getReferenceById(businessId);
		List<Product> products = new ArrayList<>(selectedBusiness.getProducts());
		return ResponseEntity.status(HttpStatus.OK).body(products);
	}

	public ResponseEntity<Product> addProduct(Long businessId, Product product){
		Product newProduct = businessRepository.findById(businessId).map(business -> {
			business.getProducts().add(product);
			return categoryRepository.findById(product.getCategoryId()).map(category -> {
				category.getProducts().add(product);
				return productRepository.save(product);
			}).orElseThrow(() -> new CategoryNotFoundException("Category Not Found"));
		}).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));
		return ResponseEntity.status(HttpStatus.OK).body(newProduct);
	}

	public ResponseEntity<Product> updateProduct(Long id, Product product){
		return productRepository.findById(id).map(product1 -> {
			product1.setProductName(product.getProductName());
			product1.setCategoryId(product.getCategoryId());
			product1.setPurchasePrice(product.getPurchasePrice());
			product1.setSellingPrice(product.getSellingPrice());
			product1.setStock(product.getStock());
			product1.setLowStockThreshold(product.getLowStockThreshold());
			product1.setExpiration(product.getExpiration());
			product1.setDaysBeforeExpiration(product.getDaysBeforeExpiration());
			Product updatedProduct = productRepository.save(product1);
			return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	public ResponseEntity<String> deleteProduct(Long id){
		Optional<Product> productOptional = productRepository.findById(id);
		if(productOptional.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product with the id of " + id + " was not found.");
		}
		productRepository.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK).body("Product with the id of " + id + " was deleted.");
	}
}
