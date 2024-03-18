package com.LuhxEn.PointOfSaleBackEnd.category;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class CategoryController {
	private final CategoryService categoryService;

	@GetMapping("/{businessId}")
	public ResponseEntity<List<CategoryDTO>> getCategories(@PathVariable Long businessId){
		return categoryService.getCategories(businessId);
	}


	@PostMapping("/{businessId}/create")
	public ResponseEntity<Category> addCategory(@PathVariable Long businessId, @RequestBody Category category){
		return categoryService.addCategory(businessId, category);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category){
		return categoryService.updateCategory(id, category);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteCategory(@PathVariable Long id){
		return categoryService.deleteCategory(id);
	}
}
