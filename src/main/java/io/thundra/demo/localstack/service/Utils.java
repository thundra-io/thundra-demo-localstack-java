package io.thundra.demo.localstack.service;

import java.util.UUID;

/**
 * @author tolga
 */
public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException("Not create instance from Utils class");
    }

    public static String generateShortUuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
