package com.LuhxEn.PointOfSaleBackEnd.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void changePassword(ChangePasswordRequest request, Principal connectedUser) {
		var user = (User) ((UsernamePasswordAuthenticationToken)connectedUser).getPrincipal();

		if(!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
			throw new IllegalStateException("Wrong Password");
		}

		if(!request.getNewPassword().equals(request.getConfirmationPassword())){
			throw new IllegalStateException("Passwords Do Not Match");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
	}
}
