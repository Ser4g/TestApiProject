package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.CreateUserResponse;
import org.example.model.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestHelper.createRandomUser;
import static org.example.utils.TestHelper.findUserByUsername;
import static org.example.utils.TestHelper.getUsers;
import static org.example.utils.TestHelper.uniqueEmail;
import static org.example.utils.TestHelper.uniqueUsername;

class UserFlowTest {

    private static final int USER_COUNT = 3;

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Чек-лист №12: create → get → пользователь присутствует с корректными данными")
    void createdUserIsPresentInList() {
        CreateUserResponse.Details details = createRandomUser(client);

        UserDto created = findUserByUsername(client, details.username());
        assertThat(created).as("Пользователь %s должен присутствовать в /user/get", details.username()).isNotNull();
        assertThat(created.username()).isEqualTo(details.username());
        assertThat(created.email()).as("email в /user/get").isEqualTo(details.email());
        assertThat(created.id()).as("id в /user/get должен совпадать с id из ответа create").isEqualTo(details.id());
        assertThat(created.password()).as("Пароль должен быть хеширован").startsWith("$2a$");
        assertThat(created.createdAt()).as("created_at").isNotBlank();
        assertThat(created.updatedAt()).as("updated_at").isNotBlank();
    }

    @Test
    @DisplayName("Чек-лист №13: Несколько регистраций — все пользователи в /user/get, id уникальны и инкрементируются")
    void multipleRegistrations() {
        List<UserDto> usersBefore = getUsers(client);
        int maxIdBefore = usersBefore.stream().mapToInt(UserDto::id).max().orElse(0);

        List<CreateUserResponse.Details> created = new ArrayList<>();
        for (int i = 0; i < USER_COUNT; i++) {
            created.add(createRandomUser(client));
        }

        List<Integer> ids = created.stream().map(CreateUserResponse.Details::id).toList();
        assertThat(ids)
                .as("id должны быть уникальны")
                .doesNotHaveDuplicates();
        assertThat(ids)
                .as("id должны инкрементироваться: каждый новый id больше максимального существовавшего (%d)", maxIdBefore)
                .allSatisfy(id -> assertThat(id).isGreaterThan(maxIdBefore));

        for (CreateUserResponse.Details details : created) {
            UserDto user = findUserByUsername(client, details.username());
            assertThat(user)
                    .as("Пользователь %s должен присутствовать в /user/get", details.username())
                    .isNotNull();
            assertThat(user.id()).as("id в /user/get для %s", details.username()).isEqualTo(details.id());
            assertThat(user.email()).as("email в /user/get для %s", details.username()).isEqualTo(details.email());
        }
    }

    @Test
    @DisplayName("Чек-лист №14: create (ошибка) → get: пользователь НЕ добавлен в список")
    void failedCreateDoesNotAddUser() {
        String username = uniqueUsername();

        Response failedResponse = client.createUser(Map.of(
                "username", username,
                "email", uniqueEmail(username)));
        assertThat(failedResponse.getStatusCode())
                .as("Регистрация без password должна завершиться ошибкой")
                .isEqualTo(400);

        assertThat(findUserByUsername(client, username))
                .as("Пользователь %s не должен быть добавлен в /user/get после неуспешной регистрации", username)
                .isNull();
    }
}
