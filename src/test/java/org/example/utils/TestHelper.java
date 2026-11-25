package org.example.utils;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.CreateUserResponse;
import org.example.model.UserDto;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public final class TestHelper {

    private TestHelper() {
    }

    public static String uniqueUsername() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String uniqueEmail(String username) {
        return username + "@test.ru";
    }

    public static String validPassword() {
        return "P@ssw0rd123";
    }

    public static CreateUserResponse.Details createRandomUser(UserApiClient client) {
        String username = uniqueUsername();
        Response response = client.createUser(username, uniqueEmail(username), validPassword());
        assertThat(response.getStatusCode())
                .as("Предусловие: создание пользователя должно пройти успешно. Тело: %s", response.asString())
                .isEqualTo(200);
        return response.as(CreateUserResponse.class).details();
    }

    public static List<UserDto> getUsers(UserApiClient client) {
        Response response = client.getUsers();
        assertThat(response.getStatusCode())
                .as("Предусловие: получение списка пользователей должно пройти успешно. Тело: %s", response.asString())
                .isEqualTo(200);
        return response.as(new TypeRef<>() {
        });
    }

    public static UserDto findUserByUsername(UserApiClient client, String username) {
        return getUsers(client).stream()
                .filter(u -> u.username().equals(username))
                .findFirst()
                .orElse(null);
    }
}
