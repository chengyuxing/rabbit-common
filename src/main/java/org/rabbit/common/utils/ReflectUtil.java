package org.rabbit.common.utils;

public final class ReflectUtil {
    public static String initGetMethod(String field, Class<?> type) {
        String prefix = "get";
        if (type == boolean.class || type == Boolean.class)
            prefix = "is";
        return prefix + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static String initSetMethod(String field) {
        return "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }
}
