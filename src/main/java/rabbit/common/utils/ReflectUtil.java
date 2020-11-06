package rabbit.common.utils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 反射工具类
 */
public final class ReflectUtil {
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
     * 获取标准javaBean的所有Set方法
     *
     * @param clazz 类
     * @return 类的set方法组
     * @throws IntrospectionException exp
     */
    public static Stream<Method> getSetMethods(Class<?> clazz) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        return Stream.of(beanInfo.getPropertyDescriptors())
                .map(PropertyDescriptor::getWriteMethod)
                .filter(Objects::nonNull);
    }

    /**
     * 获取标准javaBean的所有Set方法
     *
     * @param clazz 类
     * @return 类的set方法组
     * @throws IntrospectionException exp
     */
    public static Stream<Method> getGetMethods(Class<?> clazz) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        return Stream.of(beanInfo.getPropertyDescriptors())
                .map(PropertyDescriptor::getReadMethod)
                .filter(Objects::nonNull);
    }
}
