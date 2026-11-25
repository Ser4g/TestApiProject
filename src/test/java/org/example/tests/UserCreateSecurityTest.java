package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestHelper.uniqueEmail;
import static org.example.utils.TestHelper.uniqueUsername;
import static org.example.utils.TestHelper.validPassword;

/**
 * Нефункциональные проверки. Раздел 2.1 Безопасность
 */
class UserCreateSecurityTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Безопасность 2.1.1: SQL-инъекция в username/email — без 500 и без влияния на БД")
    void sqlInjectionIsHandledSafely() {
        String markerUsername = uniqueUsername();
        Response marker = client.createUser(markerUsername, uniqueEmail(markerUsername), validPassword());
        assertThat(marker.getStatusCode()).as("Создание маркерного пользователя должно пройти успешно").isEqualTo(200);

        Response injectionInUsername = client.createUser(Map.of(
                "username", "admin'--",
                "email", uniqueEmail(uniqueUsername()),
                "password", validPassword()));
        assertThat(injectionInUsername.getStatusCode())
                .as("SQL-инъекция в username не должна приводить к 500. Тело: %s", injectionInUsername.asString())
                .isNotEqualTo(500);

        Response injectionInEmail = client.createUser(Map.of(
                "username", uniqueUsername(),
                "email", "'; TRUNCATE TABLE users;--",
                "password", validPassword()));
        assertThat(injectionInEmail.getStatusCode())
                .as("SQL-инъекция в email не должна приводить к 500. Тело: %s", injectionInEmail.asString())
                .isNotEqualTo(500);

        Response users = client.getUsers();
        assertThat(users.getStatusCode())
                .as("После попыток SQL-инъекций /user/get должен отвечать 200 — таблица users не должна быть повреждена")
                .isEqualTo(200);
        UserDto[] list = users.as(UserDto[].class);
        assertThat(list).as("Ответ /user/get должен оставаться корректным JSON-массивом").isNotNull();

        boolean markerStillExists = Arrays.stream(list)
                .anyMatch(u -> u.username().equals(markerUsername));
        assertThat(markerStillExists)
                .as("Маркерный пользователь %s должен остаться в БД — данные не должны быть удалены инъекцией", markerUsername)
                .isTrue();
    }
}
