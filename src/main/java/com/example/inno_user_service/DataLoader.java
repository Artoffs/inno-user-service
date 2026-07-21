package com.example.inno_user_service;

import com.example.inno_user_service.entity.PaymentCard;
import com.example.inno_user_service.entity.User;
import com.example.inno_user_service.dao.PaymentCardDao;
import com.example.inno_user_service.dao.UserDao;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserDao userRepository;
    private final PaymentCardDao paymentCardRepository;
    private final Faker faker = new Faker(new Locale("ru"));

    @Override
    @Transactional
    public void run(String... args) {

        // Проверяем, есть ли уже данные
        if (userRepository.count() > 0) {
            System.out.println("✅ Данные уже есть в базе. Пропускаем заполнение.");
            System.out.println("📊 Всего пользователей: " + userRepository.count());
            System.out.println("📊 Всего карт: " + paymentCardRepository.count());
            return;
        }

        generateData();
    }

    @Transactional
    public void generateData() {
        System.out.println("📝 Начинаем заполнение базы тестовыми данными...");

        List<User> users = new ArrayList<>();
        List<PaymentCard> cards = new ArrayList<>();

        // Генерируем 10 пользователей
        for (int i = 0; i < 10; i++) {
            User user = createUser();
            users.add(user);
        }

        // Сохраняем пользователей
        userRepository.saveAll(users);
        System.out.println("✅ Создано пользователей: " + users.size());

        // Для каждого пользователя создаем 1-3 карты
        for (User user : users) {
            int cardsCount = faker.number().numberBetween(1, 4);
            for (int j = 0; j < cardsCount; j++) {
                PaymentCard card = createPaymentCard(user);
                cards.add(card);
            }
            System.out.println("👤 Пользователь " + user.getName() + " " + user.getSurname() +
                    " → карт: " + cardsCount);
        }

        // Сохраняем карты
        paymentCardRepository.saveAll(cards);

        System.out.println("✅ Создано карт: " + cards.size());
        System.out.println("🎉 База данных успешно заполнена!");
        System.out.println("📊 Итого: " + userRepository.count() + " пользователей, " +
                paymentCardRepository.count() + " карт");
    }

    public User createUser() {
        User user = new User();
        user.setName(faker.name().firstName());
        user.setSurname(faker.name().lastName());

        LocalDate birthDate = faker.date().birthday(18, 60)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        user.setBirthDate(birthDate);

        user.setEmail(faker.internet().emailAddress());
        user.setActive(true);

        return user;
    }


    public PaymentCard createPaymentCard(User user) {
        PaymentCard card = new PaymentCard();
        card.setUser(user);

        // Генерируем номер карты (16 цифр)
        String cardNumber = faker.finance().creditCard();
        card.setNumber(cardNumber);

        // Держатель карты
        String holder = user.getName().toUpperCase() + " " + user.getSurname().toUpperCase();
        card.setHolder(holder);

        // Срок действия (от 1 до 5 лет)
        LocalDate expirationDate = LocalDate.now().plusYears(faker.number().numberBetween(1, 6));
        card.setExpirationDate(expirationDate);

        // Активна или нет (80% активны)
        card.setActive(faker.bool().bool());

        return card;
    }
}