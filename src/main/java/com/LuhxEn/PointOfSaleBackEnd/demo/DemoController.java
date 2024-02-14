package com.LuhxEn.PointOfSaleBackEnd.demo;

import com.LuhxEn.PointOfSaleBackEnd.config.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.jsonwebtoken.ExpiredJwtException;

@RestController
@RequestMapping("/api/v1/demo-controller")
@RequiredArgsConstructor
public class DemoController {
	private final JwtService jwtService;

	@GetMapping
	public ResponseEntity<String> sayHello(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		String jwt = authHeader.substring(7);
		String id = jwtService.extractId(jwt);
		return ResponseEntity.ok("User Id is " + id);
	}

}
