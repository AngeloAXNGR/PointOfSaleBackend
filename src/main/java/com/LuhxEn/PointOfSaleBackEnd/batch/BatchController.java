package com.LuhxEn.PointOfSaleBackEnd.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BatchController {
	private final BatchService batchService;


	@GetMapping("/{businessId}/wastageProducts")
	public ResponseEntity<BatchDTO.WastageProducts> getWastageProducts(@PathVariable Long businessId){
		return batchService.getWastageProducts(businessId);
	}
}
