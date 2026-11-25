package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateUserResponse(
        boolean success,
        Details details,
        List<String> message
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Details(
            String username,
            String email,
            String password,
            String created_at,
            String updated_at,
            Integer id
    ) {
    }
}
