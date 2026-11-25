package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.CreateUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Чек-лист №1: Успешная регистрация с валидными username, email, password
 * → 200, success=true, details с полями id, username, email, password, created_at, updated_at
 */
class UserCreateTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Успешная регистрация с валидными username, email, password")
    void successfulRegistration() {
        String username = "test_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@test.ru";
        String password = "P@ssw0rd123";

        Response response = client.createUser(username, email, password);

        assertThat(response.getStatusCode())
                .as("Ожидался статус 200, получен %d. Тело ответа: %s", response.getStatusCode(), response.asString())
                .isEqualTo(200);

        CreateUserResponse body = response.as(CreateUserResponse.class);

        assertThat(body.success()).as("success должен быть true").isTrue();

        CreateUserResponse.Details details = body.details();
        assertThat(details).as("details должен присутствовать в ответе").isNotNull();
        assertThat(details.username()).as("username в details").isEqualTo(username);
        assertThat(details.email()).as("email в details").isEqualTo(email);
        assertThat(details.password()).as("password в details").isNotBlank();
        assertThat(details.id()).as("id в details").isNotNull().isPositive();
        assertThat(details.created_at()).as("created_at в details").isNotBlank();
        assertThat(details.updated_at()).as("updated_at в details").isNotBlank();
    }
}
