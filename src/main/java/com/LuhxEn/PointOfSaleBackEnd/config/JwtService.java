package com.LuhxEn.PointOfSaleBackEnd.config;


import com.LuhxEn.PointOfSaleBackEnd.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
	@Value("${application.security.jwt.secret-key}")
	private String secretKey;

	@Value("${application.security.jwt.expiration}")
	private Long jwtExpiration;

	@Value("${application.security.jwt.refresh-token.expiration}")
	private Long refreshExpiration;


	// ========================================Claims Related Methods================================================

	public String extractUsername(String token){
		return extractClaim(token, Claims::getSubject);
	}

	public String extractId(String token){
		return extractClaim(token, claims -> String.valueOf(claims.get("userId")));
	}
	private Claims extractAllClaims(String token){
		return Jwts
			// signing key is crucial in order to decode a token,
			// upon decoding a token, we can extract claims
			// claims are basically information tied to a user (a.k.a subject)
			// claims are also where you get to extract the username (email in this case)
			.parserBuilder()
			.setSigningKey(getSignInKey())
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	// Generic Method to extract one claim
	// <T> = method can work with any type
	// T = return type is basically 'any'
	// Function<Claims, T> = takes in a Claims Object, return 'any' type as denoted by T
	// This function is called as claimsResolver
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}



	private Key getSignInKey(){
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	//======================================Token Generation Related Methods==========================================

	// FOR TESTING
	public String generateToken2(User userDetails){
		return generateToken2(new HashMap<>(), userDetails);
	}
	public String generateToken2(
		Map<String, Object> extraClaims,
		User userDetails
	){
		return buildToken(extraClaims, userDetails, 86400000L);
	}

	// generateToken without needing to put in additional information for claims
	public String generateToken(User userDetails){
		return generateToken(new HashMap<>(), userDetails);
	}
	public String generateToken(
		Map<String, Object> extraClaims,
		User userDetails
	){
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}

	public String generateRefreshToken(
		User userDetails
	){
		return buildToken(new HashMap<>(), userDetails, refreshExpiration);
	}

	private String buildToken(
		// used to pass any additional information (claims) about our subject then storing it to the token
		// use map here since claims are in key value pairs (e.g {userId: id})
		Map<String, Object> extraClaims,
		// User type (from <projectName>.user.User NOT in security.core)
		// can use typeof  UserDetails here but we can also use User because User implements UserDetails
		User userDetails,
		Long expiration
	){
		String id = String.valueOf(userDetails.getId());
		extraClaims.put("userId", id);
		return Jwts
			.builder()
			.setClaims(extraClaims)
			.setSubject(userDetails.getUsername())
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + expiration))

			// sign token with a signing key and a specific signature algo
			.signWith(getSignInKey(), SignatureAlgorithm.HS256)

			// .compact will generate and return the string token
			.compact();
	}

	//=================================Token Validation Related Methods=================================================
	public boolean isTokenValid(String token, User userDetails){
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token){
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token){
		return extractClaim(token, Claims::getExpiration);
	}
}
