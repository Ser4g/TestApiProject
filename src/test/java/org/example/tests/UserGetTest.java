package org.example.tests;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestHelper.createRandomUser;
import static org.example.utils.TestHelper.getUsers;
import static org.example.utils.TestHelper.uniqueEmail;
import static org.example.utils.TestHelper.uniqueUsername;
import static org.example.utils.TestHelper.validPassword;

class UserGetTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Чек-лист №9: GET /user/get → 200, возвращается массив пользователей")
    void getUsersReturnsArray() {
        Response response = client.getUsers();

        assertThat(response.getStatusCode())
                .as("Ожидался статус 200, получен %d. Тело ответа: %s", response.getStatusCode(), response.asString())
                .isEqualTo(200);

        List<UserDto> users = response.as(new TypeRef<>() {
        });
        assertThat(users).as("Ответ должен быть JSON-массивом").isNotNull();
    }

    @Test
    @DisplayName("Чек-лист №10: Структура объекта пользователя — все поля присутствуют")
    void userObjectHasAllFields() {
        createRandomUser(client);

        List<UserDto> users = getUsers(client);
        assertThat(users).as("Список пользователей не должен быть пустым").isNotEmpty();

        assertThat(users).allSatisfy(user -> {
            assertThat(user.id()).as("id пользователя %s", user.username()).isNotNull();
            assertThat(user.username()).as("username пользователя id=%s", user.id()).isNotNull();
            assertThat(user.email()).as("email пользователя id=%s", user.id()).isNotNull();
            assertThat(user.password()).as("password пользователя id=%s", user.id()).isNotNull();
            assertThat(user.createdAt()).as("created_at пользователя id=%s", user.id()).isNotNull();
            assertThat(user.updatedAt()).as("updated_at пользователя id=%s", user.id()).isNotNull();
        });
    }

    @Test
    @DisplayName("Чек-лист №11: Пароль в списке хеширован, не возвращается в открытом виде")
    void passwordsInListAreHashed() {
        String username = uniqueUsername();
        String password = validPassword();
        Response createResponse = client.createUser(username, uniqueEmail(username), password);
        assertThat(createResponse.getStatusCode()).as("Регистрация должна пройти успешно").isEqualTo(200);

        List<UserDto> users = getUsers(client);
        assertThat(users).as("Список пользователей не должен быть пустым").isNotEmpty();

        assertThat(users)
                .as("Ни один пароль в списке не должен совпадать с отправленным «%s» и должен быть bcrypt-хешем", password)
                .allSatisfy(user -> assertThat(user.password())
                        .isNotEqualTo(password)
                        .startsWith("$2a$"));
    }
}
