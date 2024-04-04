package com.LuhxEn.PointOfSaleBackEnd.category;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.product.Product;
import com.LuhxEn.PointOfSaleBackEnd.product.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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
	private final ProductRepository productRepository;

	private final EntityManager entityManager;
//	public ResponseEntity<List<CategoryDTO>> getCategories(Long businessId){
//		Business selectedBusiness = businessRepository.getReferenceById(businessId);
//		List<CategoryDTO> categories = selectedBusiness.getCategories().stream()
//			.map(this::mapToCategoryDTO)
//			.collect(Collectors.toList());
//
//		return ResponseEntity.status(HttpStatus.OK).body(categories);
//	}

	public ResponseEntity<List<CategoryDTO>> getCategories(Long businessId){
		Query query = entityManager.createNativeQuery(
			"SELECT * FROM Category WHERE business_id = :businessId AND is_deleted = false", Category.class
		);

		query.setParameter("businessId", businessId);

		@SuppressWarnings("unchecked")
		List<Category> categories = query.getResultList();

		List<CategoryDTO> categoryDTOS = categories
			.stream()
			.map(this::mapToCategoryDTO)
			.toList();


		return ResponseEntity.status(HttpStatus.OK).body(categoryDTOS);
	}


	public ResponseEntity<Category> addCategory(Long businessId, Category category){
		Category newCategory = businessRepository.findById(businessId).map(business -> {
			business.getCategories().add(category);
			return categoryRepository.save(category);
		}).orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		return ResponseEntity.status(HttpStatus.OK).body(newCategory);
	}


	public ResponseEntity<Category> updateCategory(Long id, Category category){
		return categoryRepository.findById(id).map(category1 -> {
			category1.setCategoryName(category.getCategoryName());
			Category updatedCategory = categoryRepository.save(category1);
			return ResponseEntity.status(HttpStatus.OK).body(updatedCategory);
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}


	public ResponseEntity<String> deleteCategory(Long id){
		// HARD DELETE
//		Optional<Category> categoryOptional = categoryRepository.findById(id);
//		if(categoryOptional.isEmpty()){
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Category with the id of " + id + " was not found.");
//		}
//
//		categoryRepository.deleteById(id);
//		return ResponseEntity.status(HttpStatus.OK).body("Category with the id of " + id + " was deleted.");

		// SOFT DELETE
		return categoryRepository.findById(id).map(category -> {
			category.setDeleted(true);
			categoryRepository.save(category);

			List<Product> products = new ArrayList<>(category.getProducts());
			products.forEach(product -> product.setDeleted(true));
			productRepository.saveAll(products);

			return ResponseEntity.status(HttpStatus.OK).body("Category with the id of " + id + " has been deleted.");
		}).orElseGet(() -> ResponseEntity.notFound().build());
	}

	private CategoryDTO mapToCategoryDTO(Category category){
		CategoryDTO categoryDTO = new CategoryDTO();
		categoryDTO.setId(category.getId());
		categoryDTO.setCategoryName(category.getCategoryName());

		return categoryDTO;
	}
}
