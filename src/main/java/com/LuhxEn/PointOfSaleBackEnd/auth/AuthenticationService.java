package com.LuhxEn.PointOfSaleBackEnd.auth;

import com.LuhxEn.PointOfSaleBackEnd.config.JwtService;
import com.LuhxEn.PointOfSaleBackEnd.exception.UserNotFoundException;
import com.LuhxEn.PointOfSaleBackEnd.token.Token;
import com.LuhxEn.PointOfSaleBackEnd.token.TokenRepository;
import com.LuhxEn.PointOfSaleBackEnd.token.TokenType;
import com.LuhxEn.PointOfSaleBackEnd.user.User;
import com.LuhxEn.PointOfSaleBackEnd.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
	private final UserRepository repository;
	private final TokenRepository tokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthenticationResponse register(RegisterRequest request, HttpServletResponse response) {
		var user = User.builder()
			.firstname(request.getFirstname())
			.lastname(request.getLastname())
			.email(request.getEmail())
			.password(passwordEncoder.encode(request.getPassword()))
			.role(request.getRole())
			.build();

		var savedUser = repository.save(user);
		var jwtToken = jwtService.generateToken(user);
		var refreshToken = jwtService.generateRefreshToken(user);
		saveUserToken(savedUser, jwtToken);

		ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
			.httpOnly(true)
			.secure(false)
			.path("/")
			.maxAge(24 * 60 * 60)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(false)
			.path("/")
			.maxAge(7 * 24 * 60 * 60)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		return AuthenticationResponse.builder()
			.accessToken(jwtToken)
			.refreshToken(refreshToken)
			.build();
	}


	public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					request.getEmail(),
					request.getPassword()
				)
			);

			var user = repository.findByEmail(request.getEmail()).orElseThrow();
			var jwtToken = jwtService.generateToken(user);
			var refreshToken = jwtService.generateRefreshToken(user);
			revokeAllUserTokens(user);
			saveUserToken(user, jwtToken);

			ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(24 * 60 * 60)
				.build();
			response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

			ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(7 * 24 * 60 * 60)
				.build();
			response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

			return AuthenticationResponse.builder()
				.accessToken(jwtToken)
				.refreshToken(refreshToken)
				.build();
		} catch (AuthenticationException e) {
			throw new UserNotFoundException("Incorrect email address or password");
		}

	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder()
			.user(user)
			.token(jwtToken)
			.tokenType(TokenType.BEARER)
			.expired(false)
			.revoked(false)
			.build();

		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
		if (validUserTokens.isEmpty())
			return;

		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});

		tokenRepository.saveAll(validUserTokens);
	}

	public void refreshToken(
		HttpServletRequest request,
		HttpServletResponse response
	) throws IOException {
		try {
			System.out.println("refreshToken method ran");
			String refreshToken = null;
			String userEmail;
			if(request.getCookies() != null){
				for(Cookie cookie: request.getCookies()){
					if(cookie.getName().equals("refreshToken")){
						refreshToken = cookie.getValue();
					}
				}
			}

			userEmail = jwtService.extractUsername(refreshToken);
			if (userEmail != null) {
				var user = this.repository.findByEmail(userEmail).orElseThrow();

				if (jwtService.isTokenValid(refreshToken, user)) {
					var accessToken = jwtService.generateToken(user);
//					var accessToken = jwtService.generateToken2(user);
					revokeAllUserTokens(user);
					saveUserToken(user, accessToken);

					ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
						.httpOnly(true)
						.secure(false)
						.path("/")
						.maxAge(24 * 60 * 60)
						.build();
					response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

					ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
						.httpOnly(true)
						.secure(false)
						.path("/")
						.maxAge(7 * 24 * 60 * 60)
						.build();
					response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());


					var authResponse = AuthenticationResponse.builder()
						.accessToken(accessToken)
						.refreshToken(refreshToken)
						.build();
					new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
				}
			}
		} catch (ExpiredJwtException e) {
			System.out.println("Expired Token");
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write("Expired Refresh Token");
			return;
		}

	}
}
