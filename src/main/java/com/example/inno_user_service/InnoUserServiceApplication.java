package com.example.inno_user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;



@SpringBootApplication
@EnableJpaAuditing
public class InnoUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InnoUserServiceApplication.class, args);

	}

}
