package org.example.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserApiClient {

    private static final String BASE_URI = System.getProperty("baseUri", "http://18.18.18.18");
    private static final int PORT = Integer.parseInt(System.getProperty("port", "3333"));

    public Response createUser(String username, String email, String password) {
        return createUser(Map.of(
                "username", username,
                "email", email,
                "password", password));
    }

    /**
     * Отправляет только те поля, которые есть в map — для проверок с отсутствующими обязательными полями.
     */
    public Response createUser(Map<String, String> formData) {
        RequestSpecification request = given()
                .baseUri(BASE_URI)
                .port(PORT)
                .contentType("multipart/form-data");
        formData.forEach(request::multiPart);
        return request.when().post("/user/create");
    }

    public Response getUsers() {
        return given()
                .baseUri(BASE_URI)
                .port(PORT)
                .when()
                .get("/user/get");
    }
}
