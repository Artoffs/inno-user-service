package com.example.inno_user_service;

import com.example.inno_user_service.dao.PaymentCardDao;
import com.example.inno_user_service.dao.UserDao;
import com.example.inno_user_service.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@Transactional
@Rollback
public class PaymentCardIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PaymentCardDao cardDao;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void cleanDatabase() {
        entityManager.clear();
        cardDao.deleteAllInBatch();
        userDao.deleteAllInBatch();
        entityManager.clear();
        entityManager.flush();
    }

    private Long createTestUser() {
        User user = new User();
        user.setName("Тест");
        user.setSurname("Пользователь");
        user.setEmail("card_test_" + System.currentTimeMillis() + "@mail.com");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setActive(true);
        return userDao.save(user).getId();
    }

    @Test
    void createCard_shouldSave() throws Exception {
        Long userId = createTestUser();

        mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "number": "1234567890123456",
                                    "holder": "JOHN SMITH",
                                    "expirationDate": "2030-01-01"
                                }
                                """.formatted(userId))
        ).andExpect(status().isCreated());

        var cards = cardDao.findAll();
        assertEquals(1, cards.size());
    }

    @Test
    void createCard_shouldThrow_whenMoreThan5Cards() throws Exception {
        Long userId = createTestUser();

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(
                    post("/api/v1/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "userId": %d,
                                        "number": "123456789012345%d",
                                        "holder": "JOHN SMITH",
                                        "expirationDate": "2030-01-01"
                                    }
                                    """.formatted(userId, i))
            ).andExpect(status().isCreated());
        }

        mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "number": "9999999999999999",
                                    "holder": "JOHN SMITH",
                                    "expirationDate": "2030-01-01"
                                }
                                """.formatted(userId))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_shouldReturnCard() throws Exception {
        Long userId = createTestUser();

        MvcResult createResult = mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "number": "1234567890123456",
                                    "holder": "JOHN SMITH",
                                    "expirationDate": "2030-01-01"
                                }
                                """.formatted(userId))
        ).andExpect(status().isCreated()).andReturn();

        Long cardId = extractId(createResult);

        mockMvc.perform(get("/api/v1/cards/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.number").value("1234567890123456"))
                .andExpect(jsonPath("$.holder").value("JOHN SMITH"));
    }

    @Test
    void getCardsByUserId_shouldReturnCards() throws Exception {
        Long userId = createTestUser();

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(
                    post("/api/v1/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "userId": %d,
                                        "number": "123456789012345%d",
                                        "holder": "JOHN SMITH",
                                        "expirationDate": "2030-01-01"
                                    }
                                    """.formatted(userId, i))
            ).andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/v1/cards/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateCard_shouldUpdateAndReturnCard() throws Exception {
        Long userId = createTestUser();

        MvcResult createResult = mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "number": "1234567890123456",
                                    "holder": "OLD HOLDER",
                                    "expirationDate": "2030-01-01"
                                }
                                """.formatted(userId))
        ).andExpect(status().isCreated()).andReturn();

        Long cardId = extractId(createResult);

        mockMvc.perform(
                        put("/api/v1/cards/{id}", cardId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "userId": %d,
                                    "number": "9999999999999999",
                                    "holder": "NEW HOLDER",
                                    "expirationDate": "2035-01-01"
                                }
                                """.formatted(userId))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value("9999999999999999"))
                .andExpect(jsonPath("$.holder").value("NEW HOLDER"));
    }

    @Test
    void activateCard_shouldSetActiveTrue() throws Exception {
        Long userId = createTestUser();

        MvcResult createResult = mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "number": "1234567890123456",
                                    "holder": "JOHN SMITH",
                                    "expirationDate": "2030-01-01",
                                    "active": false
                                }
                                """.formatted(userId))
        ).andExpect(status().isCreated()).andReturn();

        Long cardId = extractId(createResult);

        mockMvc.perform(patch("/api/v1/cards/{id}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateCard_shouldSetActiveFalse() throws Exception {
        Long userId = createTestUser();

        MvcResult createResult = mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "number": "1234567890123456",
                                    "holder": "JOHN SMITH",
                                    "expirationDate": "2030-01-01",
                                    "active": true
                                }
                                """.formatted(userId))
        ).andExpect(status().isCreated()).andReturn();

        Long cardId = extractId(createResult);

        mockMvc.perform(patch("/api/v1/cards/{id}/deactivate", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteCard_shouldRemoveCard() throws Exception {
        Long userId = createTestUser();

        MvcResult createResult = mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": %d,
                                    "number": "1234567890123456",
                                    "holder": "JOHN SMITH",
                                    "expirationDate": "2030-01-01"
                                }
                                """.formatted(userId))
        ).andExpect(status().isCreated()).andReturn();

        Long cardId = extractId(createResult);

        mockMvc.perform(delete("/api/v1/cards/{id}", cardId))
                .andExpect(status().isNoContent());

        assertFalse(cardDao.existsById(cardId));
    }


    @Test
    void createCard_shouldThrowNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(
                post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 99999,
                                    "number": "1234567890123456",
                                    "holder": "JOHN SMITH",
                                    "expirationDate": "2030-01-01"
                                }
                                """)
        ).andExpect(status().isNotFound());
    }

    private Long extractId(MvcResult result) throws Exception {
        String json = result.getResponse().getContentAsString();
        int idIndex = json.indexOf("\"id\":");
        if (idIndex == -1) return null;
        int start = idIndex + 5;
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Long.parseLong(json.substring(start, end).trim());
    }
}
