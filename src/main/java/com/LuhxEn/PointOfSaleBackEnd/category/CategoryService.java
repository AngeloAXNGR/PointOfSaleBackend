package com.LuhxEn.PointOfSaleBackEnd.category;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final BusinessRepository businessRepository;
	public ResponseEntity<List<CategoryDTO>> getCategories(@PathVariable Long businessId){
		Business selectedBusiness = businessRepository.getReferenceById(businessId);
		List<CategoryDTO> categories = selectedBusiness.getCategories().stream()
			.map(this::mapToCategoryDTO)
			.collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(categories);
	}


	public ResponseEntity<Category> addCategory(@PathVariable Long businessId, @RequestBody Category category){
		Category newCategory = businessRepository.findById(businessId).map(business -> {
			business.getCategories().add(category);
			return categoryRepository.save(category);
		}).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		return ResponseEntity.status(HttpStatus.OK).body(newCategory);
	}


	public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category){
		return categoryRepository.findById(id).map(category1 -> {
			category1.setCategoryName(category.getCategoryName());
			Category updatedCategory = categoryRepository.save(category1);
			return ResponseEntity.status(HttpStatus.OK).body(updatedCategory);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}


	public ResponseEntity<String> deleteCategory(@PathVariable Long id){
		Optional<Category> categoryOptional = categoryRepository.findById(id);
		if(categoryOptional.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category with the id of " + id + " was not found.");
		}

		categoryRepository.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK).body("Category with the id of " + id + " was deleted.");
	}

	private CategoryDTO mapToCategoryDTO(Category category){
		CategoryDTO categoryDTO = new CategoryDTO();
		categoryDTO.setId(category.getId());
		categoryDTO.setCategoryName(category.getCategoryName());

		return categoryDTO;
	}
}
