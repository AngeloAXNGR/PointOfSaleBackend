package com.LuhxEn.PointOfSaleBackEnd.business;

import com.LuhxEn.PointOfSaleBackEnd.config.JwtService;
import com.LuhxEn.PointOfSaleBackEnd.user.User;
import com.LuhxEn.PointOfSaleBackEnd.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BusinessController {
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final BusinessRepository businessRepository;

	@GetMapping
	public ResponseEntity<List<Business>> all(HttpServletRequest request){
		String authHeader = request.getHeader("Authorization");
		String jwt = authHeader.substring(7);
		Long userId = Long.valueOf(jwtService.extractId(jwt));
		User user = userRepository.getReferenceById(userId);
		return ResponseEntity.status(HttpStatus.OK).body(businessRepository.findByUser(user));
	}

	@PostMapping("/create")
	public ResponseEntity<Business> addBusiness(HttpServletRequest request, @RequestBody Business business){
		String authHeader = request.getHeader("Authorization");
		String jwt = authHeader.substring(7);
		Long userId = Long.valueOf(jwtService.extractId(jwt));
		User user = userRepository.getReferenceById(userId);
		var newBusiness = Business
			.builder()
			.businessName(business.getBusinessName())
			.address(business.getAddress())
			.contactNumber(business.getContactNumber())
			.user(user)
			.build();

		businessRepository.save(newBusiness);
		return ResponseEntity.status(HttpStatus.OK).body(newBusiness);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<Business> updateBusiness(@PathVariable Long id, @RequestBody Business business){
		return businessRepository.findById(id).map(business1 -> {
			business1.setBusinessName(business.getBusinessName());
			business1.setAddress(business.getAddress());
			business1.setContactNumber(business.getContactNumber());
			Business updatedBusiness = businessRepository.save(business1);

			return ResponseEntity.status(HttpStatus.OK).body(updatedBusiness);
		}).orElseGet(() -> ResponseEntity.notFound().build());

	}


	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> deleteBusiness(@PathVariable Long id){
		businessRepository.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK).body("Business with the id of: " + id + " was deleted");
	}

}
