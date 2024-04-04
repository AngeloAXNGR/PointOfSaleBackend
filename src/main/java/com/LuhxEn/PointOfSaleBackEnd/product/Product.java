package com.LuhxEn.PointOfSaleBackEnd.product;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String productName;

	// explicitly name as category_id
	// for @JoinColumn category_id in the Category class
	@Column(name = "category_id")
	private Long categoryId;

	private double purchasePrice;
	private double sellingPrice;
	private int stock;
	private int lowStockThreshold;

	// YYYY-MM-DD
	private Date expiration;
	private int daysBeforeExpiration;

	private boolean isDeleted;

}
