package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(
        Integer id,
        String username,
        String email,
        String password,
        String created_at,
        String updated_at
) {
}
