package com.github.chengyuxing.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Jackson反射实例工具
 */
public final class Jackson {
    private static Object objectMapper;

    /**
     * 获取一个jackson的ObjectMapper对象
     *
     * @return ObjectMapper
     * @throws ClassNotFoundException 没有找到ObjectMapper
     * @throws IllegalAccessException 非法访问
     * @throws InstantiationException 实例化失败
     */
    public static Object getObjectMapper() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // it's not necessary use sync block
        if (objectMapper == null) {
            Class<?> jacksonClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            objectMapper = jacksonClass.newInstance();
        }
        return objectMapper;
    }

    /**
     * 对象转json字符串
     *
     * @param obj 对象
     * @return json
     */
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
            throw new RuntimeException("convert to json error: ", e);
        }
    }

    /**
     * json字符串转对象
     *
     * @param json       json
     * @param targetType 目标类型
     * @return 对象
     */
    public static Object toObject(String json, Class<?> targetType) {
        try {
            Object mapper = getObjectMapper();
            Method method = mapper.getClass().getDeclaredMethod("readValue", String.class, Class.class);
            return method.invoke(mapper, json, targetType);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException("convert to json error: ", e);
        }
    }
}