package com.LuhxEn.PointOfSaleBackEnd;

import com.LuhxEn.PointOfSaleBackEnd.auth.AuthenticationService;
import com.LuhxEn.PointOfSaleBackEnd.auth.RegisterRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.LuhxEn.PointOfSaleBackEnd.user.Role.ADMIN;
import static com.LuhxEn.PointOfSaleBackEnd.user.Role.MANAGER;

@SpringBootApplication
public class PointOfSaleBackEndApplication {

	public static void main(String[] args) {
		SpringApplication.run(PointOfSaleBackEndApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner commandLineRunner(
//		AuthenticationService service
//	) {
//		return args -> {
//			var admin = RegisterRequest.builder()
//				.firstname("Admin")
//				.lastname("Admin")
//				.email("admin@mail.com")
//				.password("password")
//				.role(ADMIN)
//				.build();
//			System.out.println("Admin token: " + service.register(admin).getAccessToken());
//
//			var manager = RegisterRequest.builder()
//				.firstname("Admin")
//				.lastname("Admin")
//				.email("manager@mail.com")
//				.password("password")
//				.role(MANAGER)
//				.build();
//			System.out.println("Manager token: " + service.register(manager).getAccessToken());
//
//		};
//	}

//	@Bean
//	public CommandLineRunner commandLineRunner(
//		AuthenticationService service
//	) {
//		return args -> {
//			var admin = RegisterRequest.builder()
//				.firstname("Admin")
//				.lastname("Admin")
//				.email("admin@mail.com")
//				.password("password")
//				.role(ADMIN)
//				.build();
//			System.out.println("Admin token: " + service.register(admin).getAccessToken());
//
//			var manager = RegisterRequest.builder()
//				.firstname("Admin")
//				.lastname("Admin")
//				.email("manager@mail.com")
//				.password("password")
//				.role(MANAGER)
//				.build();
//			System.out.println("Manager token: " + service.register(manager).getAccessToken());
//
//			var user = RegisterRequest.builder()
//				.firstname("angelo")
//				.lastname("santos")
//				.email("Angelo@testemail.com")
//				.password("123")
//				.role(ADMIN)
//				.build();
//
//			System.out.println("Admin token: " + service.register(user).getAccessToken());
//
//			var user2 = RegisterRequest.builder()
//				.firstname("a")
//				.lastname("s")
//				.email("a@a")
//				.password("123")
//				.role(ADMIN)
//				.build();
//
//			System.out.println("Admin token: " + service.register(user2).getAccessToken());
//
//		};
//	}

}
