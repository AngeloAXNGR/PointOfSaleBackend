package com.LuhxEn.PointOfSaleBackEnd.auth;

import com.LuhxEn.PointOfSaleBackEnd.config.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthenticationController {
	private final AuthenticationService service;
	private final LogoutService logoutService;

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response){
		return ResponseEntity.ok(service.register(request, response));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(
		@RequestBody AuthenticationRequest request,
		HttpServletResponse response
	) {
		return ResponseEntity.ok(service.authenticate(request, response));
	}

	@PostMapping("/refresh-token")
	public void refreshToken(
		HttpServletRequest request,
		HttpServletResponse response
	) throws IOException {
		service.refreshToken(request, response);
	}

	@PostMapping("/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication){
		logoutService.logout(request, response, authentication);
	}
}
