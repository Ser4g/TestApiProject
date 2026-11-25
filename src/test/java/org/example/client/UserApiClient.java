package org.example.client;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserApiClient {

    private static final String BASE_URI = System.getProperty("baseUri", "http://localhost");
    private static final int PORT = Integer.parseInt(System.getProperty("port", "3333"));

    private final RequestSpecification baseSpec;

    public UserApiClient() {
        this.baseSpec = given()
                .baseUri(BASE_URI)
                .port(PORT);
    }

    public Response createUser(String username, String email, String password) {
        return createUser(Map.of(
                "username", username,
                "email", email,
                "password", password));
    }

    public Response createUser(Map<String, String> formData) {
        RequestSpecification request = given()
                .spec(baseSpec)
                .contentType("multipart/form-data");
        formData.forEach(request::multiPart);
        return request.when().post("/user/create");
    }

    public Response getUsers() {
        return given()
                .spec(baseSpec)
                .when()
                .get("/user/get");
    }
}
