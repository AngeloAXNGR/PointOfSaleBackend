package com.LuhxEn.PointOfSaleBackEnd.config;

import com.LuhxEn.PointOfSaleBackEnd.token.TokenRepository;
import com.LuhxEn.PointOfSaleBackEnd.user.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final UserDetailsService userDetailsService;
	private final JwtService jwtService;
	private final TokenRepository tokenRepository;


	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		try{
			// check if we are on a valid auth endpoint, if so proceed to next request chain (or filter chain)
			if(request.getServletPath().contains("/api/v1/auth")){
				filterChain.doFilter(request, response);
				return;
			}

			// getting request header details
			final String authHeader = request.getHeader("Authorization");
			final String jwt;
			final String userEmail;

			// proceed to next request chain if request header is null OR if request header authorization starting string is not 'Bearer '
			if(authHeader == null || !authHeader.startsWith("Bearer ")){
				filterChain.doFilter(request, response);
				return;
			}

			// Extract the jwt after the string 'Bearer '
			jwt = authHeader.substring(7);

			// get username from jwt claims
			userEmail = jwtService.extractUsername(jwt);

			// check if user exists and if it is not yet logged in
			//SecurityContextHolder.getContext().getAuthentication() returns an authentication object
			// this authentication object contains various details such as the principal (subject) details
			// some of these details would include properties seen in the UserDetails object
			if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
				// get matching user from the database based on provided email
				User userDetails = (User) this.userDetailsService.loadUserByUsername(userEmail);

				// find matching token within the database
				// return true if it is not expired or revoked
				// otherwise, return false
				var isTokenValid = tokenRepository.findByToken(jwt)
					.map(t -> !t.isExpired() && !t.isRevoked())
					.orElse(false);

				// if user and token valid
				// update auth token (update security context) in a way that the token can be usable on our app's authenticated api endpoints
				if(jwtService.isTokenValid(jwt, userDetails) && isTokenValid){
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
					authToken.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request)
					);
					// updating the authentication object with properties coming from the supplied userDetails object
					SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			}
			filterChain.doFilter(request, response);
		}catch(ExpiredJwtException e){
			System.out.println("Expired Token");
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Expired JWT Token");
			return;
		}


	}
}
