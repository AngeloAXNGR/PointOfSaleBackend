package com.LuhxEn.PointOfSaleBackEnd.token;

import com.LuhxEn.PointOfSaleBackEnd.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long Id;

	@Column(unique = true)
	public String token;

	@Enumerated(EnumType.STRING)
	public TokenType tokenType = TokenType.BEARER;

	public boolean revoked;

	public boolean expired;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	public User user;
}
