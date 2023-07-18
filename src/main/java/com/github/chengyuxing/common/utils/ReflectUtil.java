package com.github.chengyuxing.common.utils;

import com.github.chengyuxing.common.tuple.Pair;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        return prefix + field.substring(0, 1).toUpperCase().concat(field.substring(1));
    }

    /**
     * 初始化set方法
     *
     * @param field 字段
     * @return set方法
     */
    public static String initSetMethod(String field) {
        return "set" + field.substring(0, 1).toUpperCase().concat(field.substring(1));
    }

    /**
     * 根据方法获取字段名
     *
     * @param clazz  class
     * @param method setter
     * @return 字段名
     * @throws NoSuchFieldException 如果没有此字段
     */
    public static Field getSetterField(Class<?> clazz, Method method) throws NoSuchFieldException {
        String mName = method.getName();
        if (mName.startsWith("set") && mName.length() > 3) {
            String name = mName.substring(3);
            name = name.substring(0, 1).toLowerCase().concat(name.substring(1));
            return clazz.getDeclaredField(name);
        }
        return null;
    }

    /**
     * 根据方法获取字段名
     *
     * @param clazz  class
     * @param method getter
     * @return 字段名
     * @throws NoSuchFieldException 如果没有此字段
     */
    public static Field getGetterField(Class<?> clazz, Method method) throws NoSuchFieldException {
        String mName = method.getName();
        if (mName.equals("getClass")) {
            return null;
        }
        String name = null;
        if (mName.startsWith("get")) {
            name = mName.substring(3);
        } else if (mName.startsWith("is")) {
            name = mName.substring(2);
        }
        if (Objects.nonNull(name) && name.length() > 0) {
            name = name.substring(0, 1).toLowerCase().concat(name.substring(1));
            return clazz.getDeclaredField(name);
        }
        return null;
    }

    /**
     * 获取标准javaBean的所有读/写方法(getter,setter)
     *
     * @param clazz 类
     * @return 类的get和set方法组
     * @throws IntrospectionException ex
     */
    public static Pair<List<Method>, List<Method>> getRWMethods(Class<?> clazz) throws IntrospectionException {
        List<Method> rs = new ArrayList<>();
        List<Method> ws = new ArrayList<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
            Method w = p.getWriteMethod();
            Method r = p.getReadMethod();
            if (w != null) {
                ws.add(w);
            }
            if (r != null) {
                rs.add(r);
            }
        }
        return Pair.of(rs, ws);
    }

    /**
     * 获取实体类指定字段的getter
     *
     * @param clazz 实体类
     * @param field 字段名
     * @return getter方法
     * @throws NoSuchMethodException 如果没有此字段的方法
     */
    public static Method getGetMethod(Class<?> clazz, Field field) throws NoSuchMethodException {
        String methodName = initGetMethod(field.getName(), field.getType());
        return clazz.getDeclaredMethod(methodName);
    }

    /**
     * 获取实体类指定字段的getter
     *
     * @param clazz 实体类
     * @param field 字段名
     * @return getter方法
     * @throws NoSuchFieldException  如果没有此字段
     * @throws NoSuchMethodException 如果没有此字段的方法
     */
    public static Method getGetMethod(Class<?> clazz, String field) throws NoSuchFieldException, NoSuchMethodException {
        Field f = clazz.getDeclaredField(field);
        return getGetMethod(clazz, f);
    }

    /**
     * 获取实体类指定字段的setter
     *
     * @param clazz 实体类
     * @param field 字段名
     * @return setter方法
     * @throws NoSuchMethodException 如果没有此字段的方法
     */
    public static Method getSetMethod(Class<?> clazz, Field field) throws NoSuchMethodException {
        String methodName = initSetMethod(field.getName());
        return clazz.getDeclaredMethod(methodName, field.getType());
    }

    /**
     * 获取实体类指定字段的setter
     *
     * @param clazz 实体类
     * @param field 字段名
     * @return setter方法
     * @throws NoSuchFieldException  如果没有此字段
     * @throws NoSuchMethodException 如果没有此字段的方法
     */
    public static Method getSetMethod(Class<?> clazz, String field) throws NoSuchFieldException, NoSuchMethodException {
        Field f = clazz.getDeclaredField(field);
        return getSetMethod(clazz, f);
    }

    /**
     * 获取一个jackson的ObjectMapper对象
     *
     * @return ObjectMapper
     * @throws ClassNotFoundException 没有找到ObjectMapper
     * @throws IllegalAccessException 非法访问
     * @throws InstantiationException 实例化失败
     * @see Jackson#getObjectMapper()
     * @deprecated
     */
    @Deprecated
    public static Object getJackson() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return Jackson.getObjectMapper();
    }

    /**
     * 对象转json字符串（需要jackson库）
     *
     * @param obj 对象
     * @return json
     * @see Jackson#toJson(Object)
     * @deprecated
     */
    @Deprecated
    public static String obj2Json(Object obj) {
        return Jackson.toJson(obj);
    }

    /**
     * json字符串转对象（需要jackson库）
     *
     * @param json       json
     * @param targetType 目标类型
     * @return 对象
     * @see Jackson#toObject(String, Class)
     * @deprecated
     */
    @Deprecated
    public static Object json2Obj(String json, Class<?> targetType) {
        return Jackson.toObject(json, targetType);
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
     * 获取指定类的一个实例
     *
     * @param clazz                 javaBean实体类
     * @param constructorParameters 构造函数参数
     * @param <T>                   实例类型参数
     * @return 类的实例
     * @throws NoSuchMethodException     如果类中没有相应的方法
     * @throws InvocationTargetException 调用目标类异常
     * @throws InstantiationException    实例化异常
     * @throws IllegalAccessException    类访问权限异常
     */
    public static <T> T getInstance(Class<T> clazz, Object... constructorParameters) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        T entity;
        if (constructorParameters.length > 0) {
            Class<?>[] classes = new Class[constructorParameters.length];
            for (int i = 0; i < constructorParameters.length; i++) {
                classes[i] = constructorParameters[i].getClass();
            }
            Constructor<T> constructor = clazz.getDeclaredConstructor(classes);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            entity = constructor.newInstance(constructorParameters);
        } else {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            entity = constructor.newInstance();
        }
        return entity;
    }
}
