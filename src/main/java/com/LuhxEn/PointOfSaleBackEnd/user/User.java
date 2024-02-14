package com.LuhxEn.PointOfSaleBackEnd.user;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import com.LuhxEn.PointOfSaleBackEnd.token.Token;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Builder
@Entity
@Data
@Table(name = "_user")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long Id;

	@NotBlank(message = "Firstname is required")
	@Size(min = 1, max = 255, message = "Firstname limit must be from 1-255")
	private String firstname;

	@NotBlank(message = "Lastname is required")
	@Size(min = 1, max = 255, message = "Lastname limit must be from 1-255")
	private String lastname;

	@Column(unique = true)
	@Size(max = 255,message = "Email max char limit must be 255")
	@Email(message = "Email must have a proper format")
	@NotBlank(message = "Email is required")
	private String email;

	@Size(min = 1, max = 255)
	@NotBlank(message = "Password is required")
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	@OneToMany(mappedBy = "user")
	private List<Token> tokens;


	@OneToMany(mappedBy = "user")
	@JsonManagedReference
	private List<Business> businesses;


	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return role.getAuthorities();
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	@Override
	public String getPassword(){
		return this.password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
