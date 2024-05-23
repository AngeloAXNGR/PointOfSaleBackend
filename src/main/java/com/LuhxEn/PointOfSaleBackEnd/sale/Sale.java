package com.LuhxEn.PointOfSaleBackEnd.sale;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Sale {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private Date transactionDate;
	private double grandTotal;
	private double recomputedGrandTotal;
	private double profit;
	private double discount;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "sale_id")
	private Set<SaleProduct> saleProduct = new HashSet<>();

}
