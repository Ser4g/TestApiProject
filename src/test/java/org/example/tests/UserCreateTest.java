package org.example.tests;

import io.restassured.response.Response;
import org.example.client.UserApiClient;
import org.example.model.CreateUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.utils.TestHelper.uniqueEmail;
import static org.example.utils.TestHelper.uniqueUsername;
import static org.example.utils.TestHelper.validPassword;

/**
 * Чек-лист №1-3: позитивные проверки /user/create
 */
class UserCreateTest {

    private final UserApiClient client = new UserApiClient();

    @Test
    @DisplayName("Чек-лист №1: Успешная регистрация с валидными username, email, password")
    void successfulRegistration() {
        String username = uniqueUsername();
        String email = uniqueEmail(username);
        String password = validPassword();

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
        assertThat(details.createdAt()).as("created_at в details").isNotBlank();
        assertThat(details.updatedAt()).as("updated_at в details").isNotBlank();
    }

    @Test
    @DisplayName("Чек-лист №2: В details возвращаются корректные username и email")
    void detailsContainSentValues() {
        String username = uniqueUsername();
        String email = uniqueEmail(username);

        Response response = client.createUser(username, email, validPassword());

        assertThat(response.getStatusCode()).isEqualTo(200);
        CreateUserResponse.Details details = response.as(CreateUserResponse.class).details();

        assertThat(details).isNotNull();
        assertThat(details.username()).as("username в details должен совпадать с отправленным").isEqualTo(username);
        assertThat(details.email()).as("email в details должен совпадать с отправленным").isEqualTo(email);
    }

    @Test
    @DisplayName("Чек-лист №3: Пароль в ответе хеширован, не возвращается в открытом виде")
    void passwordIsHashed() {
        String username = uniqueUsername();
        String password = validPassword();

        Response response = client.createUser(username, uniqueEmail(username), password);

        assertThat(response.getStatusCode()).isEqualTo(200);
        CreateUserResponse.Details details = response.as(CreateUserResponse.class).details();

        assertThat(details).isNotNull();
        assertThat(details.password())
                .as("Пароль не должен совпадать с отправленным в открытом виде")
                .isNotEqualTo(password);
        assertThat(details.password())
                .as("Пароль должен быть хеширован bcrypt (начинаться с $2a$)")
                .startsWith("$2a$");
    }
}
