package com.LuhxEn.PointOfSaleBackEnd.product;

import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.category.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {
	private final ProductService productService;

	@GetMapping("/{businessId}")
	public ResponseEntity<List<Product>> getProducts(@PathVariable Long businessId){
		return productService.getProducts(businessId);
	}

	@GetMapping("/{businessId}/paginated")
	public ResponseEntity<ProductResponse> getProductsPaginated(@PathVariable Long businessId
		, @RequestParam(defaultValue = "0") int page
		, @RequestParam(defaultValue = "0") int size
	){
		return productService.getProductsPaginated(businessId, page, size);
	}



	@PostMapping("/{businessId}/create")
	public ResponseEntity<?> addProducts(@PathVariable Long businessId, @RequestBody List<ProductDTO.ProductRequest> products){
		return productService.addProducts(businessId, products);
	}

	@PatchMapping("/update/{id}")
	public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product){
		return productService.updateProduct(id,product);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteProduct(@PathVariable Long id){
		return productService.deleteProduct(id);
	}

}
