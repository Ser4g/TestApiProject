package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.CreateUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestHelper.uniqueEmail;
import static org.example.utils.TestHelper.uniqueUsername;
import static org.example.utils.TestHelper.validPassword;

/**
 * Чек-лист №4-9: негативные проверки /user/create
 */
class UserCreateNegativeTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Чек-лист №4: Отсутствует поле username → 400, «A username is required»")
    void missingUsername() {
        Response response = client.createUser(Map.of(
                "email", uniqueEmail(uniqueUsername()),
                "password", validPassword()));

        assertBadRequest(response, "A username is required");
    }

    @Test
    @DisplayName("Чек-лист №5: Отсутствует поле email → 400, «An Email is required»")
    void missingEmail() {
        Response response = client.createUser(Map.of(
                "username", uniqueUsername(),
                "password", validPassword()));

        assertBadRequest(response, "An Email is required");
    }

    @Test
    @DisplayName("Чек-лист №6: Отсутствует поле password → 400, «A password is required»")
    void missingPassword() {
        Response response = client.createUser(Map.of(
                "username", uniqueUsername(),
                "email", uniqueEmail(uniqueUsername())));

        assertBadRequest(response, "A password is required");
    }

    @Test
    @DisplayName("Чек-лист №7: Все три поля отсутствуют → 400, «A username is required»")
    void allFieldsMissing() {
        Response response = client.createUser(Map.of());

        assertBadRequest(response, "A username is required");
    }

    @Test
    @DisplayName("Чек-лист №8: Регистрация с уже занятым username → 400, «This username is taken. Try another.»")
    void duplicateUsername() {
        String username = uniqueUsername();
        Response first = client.createUser(username, uniqueEmail(username), validPassword());
        assertThat(first.getStatusCode()).as("Первичная регистрация должна пройти успешно").isEqualTo(200);

        Response second = client.createUser(username, uniqueEmail(uniqueUsername()), validPassword());

        assertBadRequest(second, "This username is taken. Try another.");
    }

    @Test
    @DisplayName("Чек-лист №9: Регистрация с уже занятым email → 400, «Email already exists»")
    void duplicateEmail() {
        String username = uniqueUsername();
        String email = uniqueEmail(username);
        Response first = client.createUser(username, email, validPassword());
        assertThat(first.getStatusCode()).as("Первичная регистрация должна пройти успешно").isEqualTo(200);

        Response second = client.createUser(uniqueUsername(), email, validPassword());

        assertBadRequest(second, "Email already exists");
    }

    private void assertBadRequest(Response response, String expectedMessage) {
        assertThat(response.getStatusCode())
                .as("Ожидался статус 400, получен %d. Тело ответа: %s", response.getStatusCode(), response.asString())
                .isEqualTo(400);

        CreateUserResponse body = response.as(CreateUserResponse.class);
        assertThat(body.success()).as("success должен быть false").isFalse();
        assertThat(body.message())
                .as("Сообщение об ошибке должно содержать «%s»", expectedMessage)
                .isNotNull()
                .contains(expectedMessage);
    }
}
