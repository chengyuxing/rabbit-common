package rabbit.common.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 反射工具类
 */
public final class ReflectUtil {
    private static Object jackson;

    /**
     * 初始化get方法
     *
     * @param field 字段
     * @param type  返回类型
     * @return get方法
     */
    public static String initGetMethod(String field, Class<?> type) {
        String prefix = "get";
        if (type == boolean.class || type == Boolean.class)
            prefix = "is";
        return prefix + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    /**
     * 初始化set方法
     *
     * @param field 字段
     * @return set方法
     */
    public static String initSetMethod(String field) {
        return "set" + field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    /**
     * 获取标准javaBean的所有写方法
     *
     * @param clazz 类
     * @return 类的set方法组
     * @throws IntrospectionException ex
     */
    public static Stream<Method> getWriteMethods(Class<?> clazz) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        return Stream.of(beanInfo.getPropertyDescriptors())
                .map(PropertyDescriptor::getWriteMethod)
                .filter(Objects::nonNull);
    }

    /**
     * 获取标准javaBean的所有读方法
     *
     * @param clazz 类
     * @return 类的set方法组
     * @throws IntrospectionException ex
     */
    public static Stream<Method> getReadMethods(Class<?> clazz) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        return Stream.of(beanInfo.getPropertyDescriptors())
                .map(PropertyDescriptor::getReadMethod)
                .filter(Objects::nonNull);
    }

    /**
     * 判断对象是否是java的基本数据类型(包括包装类型)
     *
     * @param value 对象
     * @return 是否是java的基本数据类型
     */
    public static boolean isBasicType(Object value) {
        if (value.getClass().isPrimitive()) {
            return true;
        }
        return value instanceof String ||
                value instanceof Boolean ||
                value instanceof Integer ||
                value instanceof Long ||
                value instanceof Short ||
                value instanceof Double ||
                value instanceof Character ||
                value instanceof Float ||
                value instanceof Byte;
    }

    /**
     * 获取一个jackson的ObjectMapper对象
     *
     * @return ObjectMapper
     * @throws ClassNotFoundException 没有找到ObjectMapper
     * @throws IllegalAccessException 非法访问
     * @throws InstantiationException 实例化失败
     */
    public static Object getJackson() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // it's not necessary use sync block
        if (jackson == null) {
            Class<?> jacksonClass = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            jackson = jacksonClass.newInstance();
        }
        return jackson;
    }

    /**
     * 对象转json字符串（需要jackson库）
     *
     * @param obj 对象
     * @return json
     */
    public static String obj2Json(Object obj) {
        try {
            Object jacksonObj = getJackson();
            Method method = jacksonObj.getClass().getDeclaredMethod("writeValueAsString", Object.class);
            Object jsonStr = method.invoke(jacksonObj, obj);
            return jsonStr.toString();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("convert to json error: ", e);
        }
    }

    /**
     * json字符串转对象（需要jackson库）
     *
     * @param json json
     * @return 对象
     */
    public static Object json2Obj(String json, Class<?> targetType) {
        try {
            Object jacksonObj = getJackson();
            Method method = jacksonObj.getClass().getDeclaredMethod("readValue", String.class, Class.class);
            return method.invoke(jacksonObj, json, targetType);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("convert to json error: ", e);
        }
    }
}
