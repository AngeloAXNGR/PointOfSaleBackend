package com.LuhxEn.PointOfSaleBackEnd.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.LuhxEn.PointOfSaleBackEnd.user.Permission.*;

@RequiredArgsConstructor
public enum Role {
	USER(Collections.emptySet()),
	ADMIN(
		Set.of(
			ADMIN_READ,
			ADMIN_UPDATE,
			ADMIN_CREATE,
			ADMIN_DELETE,
			MANAGER_READ,
			MANAGER_UPDATE,
			MANAGER_CREATE,
			MANAGER_DELETE
		)
	),

	MANAGER(
		Set.of(
			MANAGER_READ,
			MANAGER_UPDATE,
			MANAGER_CREATE,
			MANAGER_DELETE
		)
	);

	@Getter
	private final Set<Permission> permissions;

	public List<SimpleGrantedAuthority> getAuthorities(){
		var authorities = getPermissions() // get enum permissions
			// similar to javascript's mapping function (should be used in conjunction with .stream())
			.stream()
			.map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
			.collect(Collectors.toList()); // return list of type SimpleGrantedAuthority
		authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
		return authorities;
	}
}
