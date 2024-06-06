package com.LuhxEn.PointOfSaleBackEnd.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = "${cors.allowed-origin}")
public class UserController {
	private final UserService userService;

	@PatchMapping
	public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal connectedUser){
		userService.changePassword(request, connectedUser);

		return ResponseEntity.status(HttpStatus.OK).body("Password Changed!");
	}
}
