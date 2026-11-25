package org.example.client;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserApiClient {

    private static final String BASE_URI = System.getProperty("baseUri", "http://18.18.18.18");
    private static final int PORT = Integer.parseInt(System.getProperty("port", "3333"));

    public Response createUser(String username, String email, String password) {
        return given()
                .baseUri(BASE_URI)
                .port(PORT)
                .contentType("multipart/form-data")
                .multiPart("username", username)
                .multiPart("email", email)
                .multiPart("password", password)
                .when()
                .post("/user/create");
    }

    public Response getUsers() {
        return given()
                .baseUri(BASE_URI)
                .port(PORT)
                .when()
                .get("/user/get");
    }
}
