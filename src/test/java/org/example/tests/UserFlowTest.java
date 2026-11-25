package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.CreateUserResponse;
import org.example.model.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestData.uniqueEmail;
import static org.example.utils.TestData.uniqueUsername;
import static org.example.utils.TestData.validPassword;

/**
 * Чек-лист №13-15: сквозные проверки create → get
 */
class UserFlowTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Чек-лист №13: create → get → пользователь присутствует с корректными данными")
    void createdUserIsPresentInList() {
        String username = uniqueUsername();
        String email = uniqueEmail(username);

        Response createResponse = client.createUser(username, email, validPassword());
        assertThat(createResponse.getStatusCode()).as("Регистрация должна пройти успешно").isEqualTo(200);
        CreateUserResponse.Details details = createResponse.as(CreateUserResponse.class).details();

        UserDto created = findUserByUsername(username);
        assertThat(created).as("Пользователь %s должен присутствовать в /user/get", username).isNotNull();
        assertThat(created.username()).isEqualTo(username);
        assertThat(created.email()).as("email в /user/get").isEqualTo(email);
        assertThat(created.id()).as("id в /user/get должен совпадать с id из ответа create").isEqualTo(details.id());
        assertThat(created.password()).as("Пароль должен быть хеширован").startsWith("$2a$");
        assertThat(created.created_at()).as("created_at").isNotBlank();
        assertThat(created.updated_at()).as("updated_at").isNotBlank();
    }

    @Test
    @DisplayName("Чек-лист №14: Несколько регистраций — все пользователи в /user/get, id уникальны и инкрементируются")
    void multipleRegistrations() {
        int usersCount = 3;
        List<CreateUserResponse.Details> created = new ArrayList<>();

        for (int i = 0; i < usersCount; i++) {
            String username = uniqueUsername();
            Response response = client.createUser(username, uniqueEmail(username), validPassword());
            assertThat(response.getStatusCode())
                    .as("Регистрация пользователя %d должна пройти успешно", i + 1)
                    .isEqualTo(200);
            created.add(response.as(CreateUserResponse.class).details());
        }

        List<Integer> ids = created.stream().map(CreateUserResponse.Details::id).toList();
        assertThat(ids)
                .as("id должны быть уникальны")
                .doesNotHaveDuplicates();
        assertThat(ids)
                .as("id должны инкрементироваться (строго возрастать)")
                .isSortedAccordingTo(Integer::compareTo);

        UserDto[] allUsers = client.getUsers().as(UserDto[].class);
        for (CreateUserResponse.Details details : created) {
            UserDto user = Arrays.stream(allUsers)
                    .filter(u -> u.username().equals(details.username()))
                    .findFirst()
                    .orElse(null);
            assertThat(user)
                    .as("Пользователь %s должен присутствовать в /user/get", details.username())
                    .isNotNull();
            assertThat(user.id()).as("id в /user/get для %s", details.username()).isEqualTo(details.id());
            assertThat(user.email()).as("email в /user/get для %s", details.username()).isEqualTo(details.email());
        }
    }

    @Test
    @DisplayName("Чек-лист №15: create (ошибка) → get: пользователь НЕ добавлен в список")
    void failedCreateDoesNotAddUser() {
        String username = uniqueUsername();

        Response failedResponse = client.createUser(Map.of(
                "username", username,
                "email", uniqueEmail(username)));
        assertThat(failedResponse.getStatusCode())
                .as("Регистрация без password должна завершиться ошибкой")
                .isEqualTo(400);

        assertThat(findUserByUsername(username))
                .as("Пользователь %s не должен быть добавлен в /user/get после неуспешной регистрации", username)
                .isNull();
    }

    private UserDto findUserByUsername(String username) {
        Response response = client.getUsers();
        assertThat(response.getStatusCode()).isEqualTo(200);
        return Arrays.stream(response.as(UserDto[].class))
                .filter(u -> u.username().equals(username))
                .findFirst()
                .orElse(null);
    }
}
