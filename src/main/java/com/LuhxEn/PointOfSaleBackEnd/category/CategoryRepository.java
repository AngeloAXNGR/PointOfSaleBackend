package com.LuhxEn.PointOfSaleBackEnd.category;

import com.LuhxEn.PointOfSaleBackEnd.business.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
//	List<Category> findByBusiness(Business business);
}
