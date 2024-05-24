package com.LuhxEn.PointOfSaleBackEnd.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.LuhxEn.PointOfSaleBackEnd.user.Permission.*;
import static com.LuhxEn.PointOfSaleBackEnd.user.Role.ADMIN;
import static com.LuhxEn.PointOfSaleBackEnd.user.Role.MANAGER;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {
	// urls the spring security will ignore
	private static final String[] WHITE_LIST_URL = {"/api/v1/auth/**",
//		"/api/v1/users/**",
		"/v2/api-docs",
		"/v3/api-docs",
		"/v3/api-docs/**",
		"/swagger-resources",
		"/swagger-resources/**",
		"/configuration/ui",
		"/configuration/security",
		"/swagger-ui/**",
		"/webjars/**",
		"/swagger-ui.html"};

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;
	private final LogoutHandler logoutHandler;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
		http
			// disable csrf verification
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(req ->
				req.requestMatchers(WHITE_LIST_URL)
					.permitAll()

					// Beside all the whitelisted urls, we enforce security on other api endpoints based on user roles
					// and permissions which the role has
					.requestMatchers("/api/v1/management/**").hasAnyRole(ADMIN.name(), MANAGER.name())
					.requestMatchers(GET, "/api/v1/management/**").hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
					.requestMatchers(POST, "/api/v1/management/**").hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
					.requestMatchers(PUT, "/api/v1/management/**").hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())
					.requestMatchers(DELETE, "/api/v1/management/**").hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())
					.anyRequest()
					.authenticated()
			)

			// we do not need any session here hence it is STATELESS (it means sessions should not be stored)
			// this approach will ensure that other endpoints would have to be authenticated
			.sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
			.authenticationProvider(authenticationProvider)

			// this is where our custom-made authentication filter is being used
			// .addFilterBefore is used as we want to execute jwtAuthFilter first before UsernamePasswordAuthenticationFilter
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)


			.logout(logout ->
				logout.logoutUrl("/api/v1/auth/logout")
					.addLogoutHandler(logoutHandler)
					.logoutSuccessHandler(((request, response, authentication) -> SecurityContextHolder.clearContext()))
			);

		return http.build();
	}
}
