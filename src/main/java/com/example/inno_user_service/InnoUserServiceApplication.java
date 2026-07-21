package com.example.inno_user_service;

import com.example.inno_user_service.dao.UserDao;
import com.example.inno_user_service.entity.PaymentCard;
import com.example.inno_user_service.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing
public class InnoUserServiceApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(InnoUserServiceApplication.class, args);
		UserDao userDao = run.getBean("userDao", UserDao.class);
		DataLoader dataLoader = run.getBean("dataLoader", DataLoader.class);
		System.out.println(userDao.findAll());

		User user = dataLoader.createUser();
		PaymentCard paymentCard = dataLoader.createPaymentCard(user);
		user.addPaymentCard(paymentCard);

		userDao.save(user);

	}

}
