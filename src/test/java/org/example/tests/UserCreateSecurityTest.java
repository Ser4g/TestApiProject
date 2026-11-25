package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestHelper.createRandomUser;
import static org.example.utils.TestHelper.findUserByUsername;
import static org.example.utils.TestHelper.getUsers;
import static org.example.utils.TestHelper.uniqueEmail;
import static org.example.utils.TestHelper.uniqueUsername;
import static org.example.utils.TestHelper.validPassword;

class UserCreateSecurityTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Безопасность 2.1.1: SQL-инъекция в username/email — без 500 и без влияния на БД")
    void sqlInjectionIsHandledSafely() {
        String markerUsername = createRandomUser(client).username();

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

        List<UserDto> list = getUsers(client);
        assertThat(list).as("Ответ /user/get должен оставаться корректным JSON-массивом").isNotNull();

        assertThat(findUserByUsername(client, markerUsername))
                .as("Маркерный пользователь %s должен остаться в БД — данные не должны быть удалены инъекцией", markerUsername)
                .isNotNull();
    }
}
