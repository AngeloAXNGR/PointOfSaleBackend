package com.LuhxEn.PointOfSaleBackEnd.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchService {
	private final BatchRepository batchRepository;


	public ResponseEntity<BatchDTO.WastageProducts> getWastageProducts(Long businessId){
		Object[] wastageProductValues = (Object[]) batchRepository.getWastageProducts(businessId);
		long wastedStocks = (Long) wastageProductValues[0];
		double wastedSum = (double) wastageProductValues[1];
		BatchDTO.WastageProducts wastageProducts = 	BatchDTO.WastageProducts
			.builder()
			.wastedStocks(wastedStocks)
			.wastedSum(wastedSum)
			.build();
		return ResponseEntity.status(HttpStatus.OK).body(wastageProducts);
	}
}
