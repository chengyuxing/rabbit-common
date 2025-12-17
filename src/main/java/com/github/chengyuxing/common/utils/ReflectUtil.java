package com.github.chengyuxing.common.utils;

import com.github.chengyuxing.common.MethodReference;
import com.github.chengyuxing.common.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflect util.
 */
public final class ReflectUtil {
    private static final Map<String, String> METHOD_REF_CACHE = new ConcurrentHashMap<>();

    public static String initGetMethod(String field, Class<?> type) {
        String prefix = "get";
        if (type == boolean.class)
            prefix = "is";
        return prefix + field.substring(0, 1).toUpperCase().concat(field.substring(1));
    }

    public static String initSetMethod(String field) {
        return "set" + field.substring(0, 1).toUpperCase().concat(field.substring(1));
    }

    /**
     * Get field name from Lambda method reference.
     *
     * @param methodRef method reference e.g. User::getName
     * @param <T>       class type
     * @return field name
     */
    public static <T> String getFieldName(@NotNull MethodReference<T> methodRef) {
        return METHOD_REF_CACHE.computeIfAbsent(methodRef.getClass().getName(), k -> {
            try {
                Method writeReplace = methodRef.getClass().getDeclaredMethod("writeReplace");
                writeReplace.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(methodRef);
                String methodName = serializedLambda.getImplMethodName();
                if (methodName.startsWith("get") && methodName.length() > 3) {
                    return Introspector.decapitalize(methodName.substring(3));
                }
                throw new IllegalArgumentException("Invalid method reference: " + methodName);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Unable to parse lambda expression", e);
            }
        });
    }

    /**
     * Get setter field.
     *
     * @param clazz  class
     * @param method setter
     * @return field
     * @throws NoSuchFieldException if field not exists
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
     * Get getter field.
     *
     * @param clazz  class
     * @param method getter
     * @return field
     * @throws NoSuchFieldException if field not exists
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
        if (Objects.nonNull(name) && !name.isEmpty()) {
            name = name.substring(0, 1).toLowerCase().concat(name.substring(1));
            return clazz.getDeclaredField(name);
        }
        return null;
    }

    /**
     * Get standard java bean all getters and setters.
     *
     * @param clazz class
     * @return pair of getters and setters
     * @throws IntrospectionException ex
     */
    public static Pair<List<Method>, List<Method>> getRWMethods(Class<?> clazz) throws IntrospectionException {
        List<Method> rs = new ArrayList<>();
        List<Method> ws = new ArrayList<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
        for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
            Method w = p.getWriteMethod();
            Method r = p.getReadMethod();
            if (Objects.nonNull(w)) {
                ws.add(w);
            }
            if (Objects.nonNull(r)) {
                if (r.getName().equals("getClass")) {
                    continue;
                }
                rs.add(r);
            }
        }
        return Pair.of(rs, ws);
    }

    /**
     * Get getter method.
     *
     * @param clazz class
     * @param field field
     * @return getter
     * @throws NoSuchMethodException if method not exists
     */
    public static Method getGetMethod(Class<?> clazz, Field field) throws NoSuchMethodException {
        String methodName = initGetMethod(field.getName(), field.getType());
        return clazz.getDeclaredMethod(methodName);
    }

    /**
     * Get getter method.
     *
     * @param clazz class
     * @param field field name
     * @return getter
     * @throws NoSuchFieldException  if field not exists
     * @throws NoSuchMethodException if method not exists
     */
    public static Method getGetMethod(Class<?> clazz, String field) throws NoSuchFieldException, NoSuchMethodException {
        Field f = clazz.getDeclaredField(field);
        return getGetMethod(clazz, f);
    }

    /**
     * Get setter method.
     *
     * @param clazz class
     * @param field field
     * @return setter
     * @throws NoSuchMethodException if method not exists
     */
    public static Method getSetMethod(Class<?> clazz, Field field) throws NoSuchMethodException {
        String methodName = initSetMethod(field.getName());
        return clazz.getDeclaredMethod(methodName, field.getType());
    }

    /**
     * Get setter method.
     *
     * @param clazz class
     * @param field field name
     * @return setter
     * @throws NoSuchFieldException  if field not exists
     * @throws NoSuchMethodException if method not exists
     */
    public static Method getSetMethod(Class<?> clazz, String field) throws NoSuchFieldException, NoSuchMethodException {
        Field f = clazz.getDeclaredField(field);
        return getSetMethod(clazz, f);
    }

    /**
     * Check is java basic data type(includes boxed type) or not.
     *
     * @param value value
     * @return true or false
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
     * Get new instance of class.
     *
     * @param clazz                 standard java bean class
     * @param constructorParameters constructor parameters
     * @param <T>                   java bean type
     * @return new instance
     * @throws NoSuchMethodException     if method not exists
     * @throws InvocationTargetException if invoke error
     * @throws InstantiationException    if construct error.
     * @throws IllegalAccessException    if access error.
     */
    public static <T> T getInstance(Class<T> clazz, Object... constructorParameters) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (constructorParameters.length > 0) {
            @SuppressWarnings("unchecked") Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
            for (Constructor<T> constructor : constructors) {
                Class<?>[] paramClasses = constructor.getParameterTypes();
                if (paramClasses.length != constructorParameters.length) {
                    continue;
                }
                boolean match = true;
                for (int i = 0; i < paramClasses.length; i++) {
                    Class<?> paramClass = paramClasses[i];
                    Object value = constructorParameters[i];
                    if (Objects.isNull(value)) {
                        continue;
                    }
                    Class<?> valueClass = value.getClass();
                    if (!paramClass.isAssignableFrom(valueClass)) {
                        match = false;
                        break;
                    }
                }
                if (!match) {
                    continue;
                }
                if (!constructor.isAccessible()) {
                    constructor.setAccessible(true);
                }
                return constructor.newInstance(constructorParameters);
            }
            StringJoiner sb = new StringJoiner(", ");
            for (Object param : constructorParameters) {
                if (Objects.isNull(param)) {
                    sb.add("null");
                    continue;
                }
                sb.add(param.getClass().getName());
            }
            throw new NoSuchMethodException(clazz.getName() + "<init>(" + sb + ")");
        }
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return constructor.newInstance();
    }
}
