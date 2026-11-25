package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestHelper.uniqueEmail;
import static org.example.utils.TestHelper.uniqueUsername;
import static org.example.utils.TestHelper.validPassword;

/**
 * Чек-лист №10-12: проверки /user/get
 */
class UserGetTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Чек-лист №10: GET /user/get → 200, возвращается массив пользователей")
    void getUsersReturnsArray() {
        Response response = client.getUsers();

        assertThat(response.getStatusCode())
                .as("Ожидался статус 200, получен %d. Тело ответа: %s", response.getStatusCode(), response.asString())
                .isEqualTo(200);

        UserDto[] users = response.as(UserDto[].class);
        assertThat(users).as("Ответ должен быть JSON-массивом").isNotNull();
    }

    @Test
    @DisplayName("Чек-лист №11: Структура объекта пользователя — все поля присутствуют")
    void userObjectHasAllFields() {
        String username = uniqueUsername();
        Response createResponse = client.createUser(username, uniqueEmail(username), validPassword());
        assertThat(createResponse.getStatusCode()).as("Регистрация должна пройти успешно").isEqualTo(200);

        UserDto[] users = client.getUsers().as(UserDto[].class);
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
    @DisplayName("Чек-лист №12: Пароль в списке хеширован, не возвращается в открытом виде")
    void passwordsInListAreHashed() {
        String username = uniqueUsername();
        String password = validPassword();
        Response createResponse = client.createUser(username, uniqueEmail(username), password);
        assertThat(createResponse.getStatusCode()).as("Регистрация должна пройти успешно").isEqualTo(200);

        UserDto[] users = client.getUsers().as(UserDto[].class);
        assertThat(users).as("Список пользователей не должен быть пустым").isNotEmpty();

        assertThat(users)
                .as("Ни один пароль в списке не должен совпадать с отправленным «%s» и должен быть bcrypt-хешем", password)
                .allSatisfy(user -> assertThat(user.password())
                        .isNotEqualTo(password)
                        .startsWith("$2a$"));
    }
}
