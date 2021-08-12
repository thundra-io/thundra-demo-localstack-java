package io.thundra.demo.localstack.service;

import java.util.UUID;

public class Utils {
    private Utils() {
        throw new IllegalStateException("Not create instance from Utils class");
    }

    public static String generateShortUuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
