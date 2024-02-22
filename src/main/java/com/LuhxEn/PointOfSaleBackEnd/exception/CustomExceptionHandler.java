package com.LuhxEn.PointOfSaleBackEnd.exception;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<Map<String,?>> handleUserNotFoundException(UserNotFoundException ex){
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String,?>> handleConstraintViolationException(ConstraintViolationException ex){
		List<String> errors = ex.getConstraintViolations().stream()
			.map(ConstraintViolation::getMessage).toList();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errors));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String,?>> handleDataIntegrityViolationException(DataIntegrityViolationException ex){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
	}

}
