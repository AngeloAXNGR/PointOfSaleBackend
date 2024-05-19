package com.LuhxEn.PointOfSaleBackEnd.batch;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.business.BusinessRepository;
import com.LuhxEn.PointOfSaleBackEnd.category.Category;
import com.LuhxEn.PointOfSaleBackEnd.exception.BusinessNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.CategoryNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.exception.ProductNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.product.Product;
import com.LuhxEn.PointOfSaleBackEnd.product.ProductDTO;
import com.LuhxEn.PointOfSaleBackEnd.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatchService {
	private final BusinessRepository businessRepository;
	private final ProductRepository productRepository;
	private final BatchRepository batchRepository;

	public ResponseEntity<BatchDTO.WastageProducts> getWastageProducts(Long businessId) {
		Object[] wastageProductValues = (Object[]) batchRepository.getWastageProducts(businessId);
		long wastedStocks = (Long) wastageProductValues[0];
		double wastedSum = (double) wastageProductValues[1];
		BatchDTO.WastageProducts wastageProducts = BatchDTO.WastageProducts
			.builder()
			.wastedStocks(wastedStocks)
			.wastedSum(wastedSum)
			.build();
		return ResponseEntity.status(HttpStatus.OK).body(wastageProducts);
	}

	public ResponseEntity<?> restockProduct(Long productId, BatchDTO.RestockProduct restockProduct) {
		Business selectedBusiness = businessRepository.findById(restockProduct.getBusinessId())
			.orElseThrow(() -> new BusinessNotFoundException("Business Not Found"));

		Product selectedProduct = productRepository.findById(productId)
			.orElseThrow(() -> new ProductNotFoundException("Product Not Found"));

		Optional<Batch> existingBatch = batchRepository.getBatch(selectedProduct.getId()).stream().filter(batch -> {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String batchDateStr = dateFormat.format(batch.getExpirationDate());
			String productDateStr = dateFormat.format(restockProduct.getExpirationDate());

			return batchDateStr.equals(productDateStr);
		}).findFirst();

		if (existingBatch.isPresent()) {
			System.out.println("BREAK POINT 3");
			Batch batchToUpdate = existingBatch.get();
			batchToUpdate.setStock(batchToUpdate.getStock() + restockProduct.getStock());
			batchRepository.save(batchToUpdate);
			selectedProduct.setTotalStock(selectedProduct.getTotalStock() + restockProduct.getStock());
			productRepository.save(selectedProduct);
			return ResponseEntity.status(HttpStatus.OK).body(selectedProduct);
		} else {
			Batch batch = Batch
				.builder()
				.expirationDate(restockProduct.getExpirationDate())
				.stock(restockProduct.getStock())
				.product(selectedProduct)
				.batchPurchasePrice(selectedProduct.getPurchasePrice())
				.business(selectedBusiness)
				.build();
			batchRepository.save(batch);
			selectedProduct.setTotalStock(selectedProduct.getTotalStock() + restockProduct.getStock());
			productRepository.save(selectedProduct);
			return ResponseEntity.status(HttpStatus.OK).body(selectedProduct);
		}

	}
}
