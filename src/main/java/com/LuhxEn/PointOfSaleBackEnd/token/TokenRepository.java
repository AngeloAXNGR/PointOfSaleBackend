package com.LuhxEn.PointOfSaleBackEnd.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
	@Query(value = """
      select t from Token t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
	List<Token> findAllValidTokenByUser(@Param("id") Long Id); // Param id = :id which is in the query annotation

	Optional<Token> findByToken(String token);
}
