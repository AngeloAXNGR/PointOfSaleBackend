package com.LuhxEn.PointOfSaleBackEnd.business;


import com.LuhxEn.PointOfSaleBackEnd.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Business {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String businessName;
	private String address;
	private String contactNumber;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private User user;
}
