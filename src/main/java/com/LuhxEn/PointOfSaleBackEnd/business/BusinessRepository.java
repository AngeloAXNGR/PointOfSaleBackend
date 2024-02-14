package com.LuhxEn.PointOfSaleBackEnd.business;

import com.LuhxEn.PointOfSaleBackEnd.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {
	List<Business> findByUser(User user);
}
