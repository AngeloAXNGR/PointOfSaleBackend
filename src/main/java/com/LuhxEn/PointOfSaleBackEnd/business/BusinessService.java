package com.LuhxEn.PointOfSaleBackEnd.business;

import com.LuhxEn.PointOfSaleBackEnd.category.Category;
import com.LuhxEn.PointOfSaleBackEnd.config.JwtService;
import com.LuhxEn.PointOfSaleBackEnd.user.User;
import com.LuhxEn.PointOfSaleBackEnd.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessService {
	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final BusinessRepository businessRepository;

	public ResponseEntity<List<BusinessDTO>> getBusinesses(HttpServletRequest request){
		User user = getUser(request);
		List<BusinessDTO> businesses = businessRepository.findByUser(user)
			.stream()
			.map(this::mapToBusinessDTO)
			.toList();
		return ResponseEntity.status(HttpStatus.OK).body(businesses);
	}

	public ResponseEntity<Business> createBusiness(HttpServletRequest request, Business business){
		User user = getUser(request);
		Business newBusiness = Business
			.builder()
			.businessName(business.getBusinessName())
			.address(business.getAddress())
			.contactNumber(business.getContactNumber())
			.user(user)
			.build();

		businessRepository.save(newBusiness);
		return ResponseEntity.status(HttpStatus.OK).body(newBusiness);
	}

	public ResponseEntity<Business> updateBusiness(@PathVariable Long id, @RequestBody Business business){
		return businessRepository.findById(id).map(business1 -> {
			business1.setBusinessName(business.getBusinessName());
			business1.setAddress(business.getAddress());
			business1.setContactNumber(business.getContactNumber());
			Business updatedBusiness = businessRepository.save(business1);

			return ResponseEntity.status(HttpStatus.OK).body(updatedBusiness);
		}).orElseGet(() -> ResponseEntity.notFound().build());

	}

	public ResponseEntity<String> deleteBusiness(@PathVariable Long id){
		Optional<Business> businessOptional = businessRepository.findById(id);
		if(businessOptional.isEmpty()){
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Business with the id of " + id + " was not found.");
		}

		businessRepository.deleteById(id);
		return ResponseEntity.status(HttpStatus.OK).body("Business with the id of " + id + " was deleted.");
	}


	private BusinessDTO mapToBusinessDTO(Business business){
		BusinessDTO businessDTO = new BusinessDTO();
		businessDTO.setId(business.getId());
		businessDTO.setBusinessName(business.getBusinessName());
		businessDTO.setAddress(business.getAddress());
		businessDTO.setContactNumber(business.getContactNumber());
		return businessDTO;
	}

	private User getUser(HttpServletRequest request){
		String authHeader = request.getHeader("Authorization");
		String jwt = authHeader.substring(7);
		Long userId = Long.valueOf(jwtService.extractId(jwt));
		return userRepository.getReferenceById(userId);
	}

}
