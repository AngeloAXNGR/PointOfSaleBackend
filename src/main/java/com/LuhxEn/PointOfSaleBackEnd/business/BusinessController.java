package com.LuhxEn.PointOfSaleBackEnd.business;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origin}")
public class BusinessController {
	private final BusinessService businessService;

	@GetMapping
	public ResponseEntity<List<BusinessDTO>> getBusinesses(HttpServletRequest request){
		return businessService.getBusinesses(request);
	}

	@PostMapping("/create")
	public ResponseEntity<Business> createBusiness(HttpServletRequest request, @RequestBody Business business){
		return businessService.createBusiness(request, business);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<Business> updateBusiness(@PathVariable Long id, @RequestBody Business business){
		return businessService.updateBusiness(id, business);

	}


	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteBusiness(@PathVariable Long id){
		return businessService.deleteBusiness(id);
	}

}
