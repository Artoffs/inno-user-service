package com.example.inno_user_service;

import com.example.inno_user_service.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class UserControllerIntegrationTest {

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

    @BeforeEach
    void cleanDatabase() {
        userDao.deleteAll();
    }

    @Test
    void createUser_shouldReturnCreated() throws Exception {
        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Иван",
                                    "surname": "Иванов",
                                    "email": "ivan@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated());
    }

    @Test
    void createUser_shouldThrow_whenEmailExists() throws Exception {
        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Первый",
                                    "surname": "Тестов",
                                    "email": "duplicate@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Второй",
                                    "surname": "Тестов",
                                    "email": "duplicate@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        MvcResult createResult = mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Тест",
                                    "surname": "Тестов",
                                    "email": "get@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated()).andReturn();

        Long userId = extractId(createResult);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Тест"))
                .andExpect(jsonPath("$.email").value("get@mail.com"));
    }

    @Test
    void getUserById_shouldReturnNotFound_whenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() throws Exception {
        MvcResult createResult = mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Старое имя",
                                    "surname": "Старая фамилия",
                                    "email": "update@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated()).andReturn();

        Long userId = extractId(createResult);

        mockMvc.perform(
                        put("/api/v1/users/{id}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "name": "Новое имя",
                                    "surname": "Новая фамилия",
                                    "email": "update@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое имя"))
                .andExpect(jsonPath("$.surname").value("Новая фамилия"));
    }

    @Test
    void updateUser_shouldThrow_whenEmailExists() throws Exception {
        MvcResult result1 = mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Первый",
                                    "surname": "Тестов",
                                    "email": "user1@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated()).andReturn();

        Long user1Id = extractId(result1);

        MvcResult result2 = mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Второй",
                                    "surname": "Тестов",
                                    "email": "user2@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated()).andReturn();

        Long user2Id = extractId(result2);

        mockMvc.perform(
                put("/api/v1/users/{id}", user2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Обновленный",
                                    "surname": "Тестов",
                                    "email": "user1@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isBadRequest());
    }

    @Test
    void activateUser_shouldSetActiveTrue() throws Exception {
        MvcResult createResult = mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Тест",
                                    "surname": "Тестов",
                                    "email": "activate@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": false
                                }
                                """)
        ).andExpect(status().isCreated()).andReturn();

        Long userId = extractId(createResult);

        mockMvc.perform(patch("/api/v1/users/{id}/activate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void deactivateUser_shouldSetActiveFalse() throws Exception {
        MvcResult createResult = mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Тест",
                                    "surname": "Тестов",
                                    "email": "deactivate@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated()).andReturn();

        Long userId = extractId(createResult);

        mockMvc.perform(patch("/api/v1/users/{id}/deactivate", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteUser_shouldRemoveUser() throws Exception {
        MvcResult createResult = mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Тест",
                                    "surname": "Тестов",
                                    "email": "delete@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated()).andReturn();

        Long userId = extractId(createResult);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_shouldReturnPage() throws Exception {
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(
                    post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "name": "User%d",
                                        "surname": "Тестов",
                                        "email": "user%d@mail.com",
                                        "birthDate": "1990-01-01",
                                        "isActive": true
                                    }
                                    """.formatted(i, i))
            ).andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void filterUsersByName_shouldReturnFiltered() throws Exception {
        // Создаем пользователей
        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Алексей",
                                    "surname": "Тестов",
                                    "email": "alexey@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Иван",
                                    "surname": "Тестов",
                                    "email": "ivan@mail.com",
                                    "birthDate": "1990-01-01",
                                    "isActive": true
                                }
                                """)
        ).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users")
                        .param("firstName", "Алексей"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Алексей"));
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
