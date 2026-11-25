package org.example.utils;

import java.util.UUID;

public final class TestHelper {

    private TestHelper() {
    }

    public static String uniqueUsername() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String uniqueEmail(String username) {
        return username + "@test.ru";
    }

    public static String validPassword() {
        return "P@ssw0rd123";
    }
}
