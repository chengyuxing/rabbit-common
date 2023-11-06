package com.github.chengyuxing.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Jackson util.
 */
public final class Jackson {
    private static Object objectMapper;

    public static Object getObjectMapper() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // it's not necessary use sync block
        if (objectMapper == null) {
            Class<?> jacksonClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            objectMapper = jacksonClass.newInstance();
        }
        return objectMapper;
    }

    public static String toJson(Object obj) {
        try {
            Object mapper = getObjectMapper();
            Method method = mapper.getClass().getDeclaredMethod("writeValueAsString", Object.class);
            Object json = method.invoke(mapper, obj);
            if (json.equals("null")) {
                return null;
            }
            return json.toString();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("convert to json error.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(String json, Class<T> targetType) {
        try {
            Object mapper = getObjectMapper();
            Method method = mapper.getClass().getDeclaredMethod("readValue", String.class, Class.class);
            return (T) method.invoke(mapper, json, targetType);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("convert to object error.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> toObjects(String json, Class<T> elementType) {
        try {
            Object mapper = getObjectMapper();
            Method readerForListOf = mapper.getClass().getDeclaredMethod("readerForListOf", Class.class);
            Object objectReader = readerForListOf.invoke(mapper, elementType);
            Method readValue = objectReader.getClass().getDeclaredMethod("readValue", String.class);
            return (List<T>) readValue.invoke(objectReader, json);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("convert to objects error.", e);
        }
    }
}
