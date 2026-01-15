package com.github.chengyuxing.common.util;

import com.github.chengyuxing.common.MethodReference;
import com.github.chengyuxing.common.PropertyMeta;
import org.jetbrains.annotations.NotNull;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reflect util.
 */
public final class ReflectUtils {
    private static final Map<String, String> METHOD_REF_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, PropertyMeta>> BEAN_PROPERTY_CACHE = new ConcurrentHashMap<>();

    /**
     * Generates the standard getter method name for a given field name and its type.
     *
     * @param name the name of the field
     * @param type the type of the field
     * @return the name of the getter method corresponding to the provided field name and type
     */
    public static String getGetterName(String name, Class<?> type) {
        String prefix = "get";
        if (type == boolean.class)
            prefix = "is";
        char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return prefix + new String(chars);
    }

    /**
     * Generates the standard setter method name for a given field name.
     *
     * @param name the name of the field
     * @return the name of the setter method corresponding to the provided field name
     */
    public static String getSetterName(String name) {
        char[] chars = name.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return "set" + new String(chars);
    }

    /**
     * Determines if the given method is a getter method.
     *
     * @param method the method to check
     * @return true if the method is a getter, false otherwise
     */
    public static boolean isGetter(Method method) {
        if (!Modifier.isPublic(Modifier.methodModifiers())) return false;
        if (method.getParameterCount() != 0) return false;
        if (method.getReturnType() == void.class) return false;

        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) return true;
        return name.startsWith("is") && name.length() > 2 && method.getReturnType() == boolean.class;
    }

    /**
     * Determines if the given method is a setter method.
     *
     * @param method the method to check
     * @return true if the method is a setter, false otherwise
     */
    public static boolean isSetter(Method method) {
        if (!Modifier.isPublic(Modifier.methodModifiers())) return false;
        if (method.getParameterCount() != 1) return false;
        if (method.getReturnType() != void.class) return false;

        String name = method.getName();
        return name.startsWith("set") && name.length() > 3;
    }

    /**
     * Determines the property name from a given getter or is method.
     *
     * @param m the method to extract the property name from, which should be a getter (get) or is (is) method
     * @return the inferred property name based on the method's name
     * @throws IllegalStateException if the provided method is not recognized as a valid getter or is method
     */
    public static String propertyName(Method m) {
        String name = m.getName();
        if (name.startsWith("get") && name.length() > 3)
            return Introspector.decapitalize(name.substring(3));
        if (name.startsWith("is") && name.length() > 2)
            return Introspector.decapitalize(name.substring(2));
        throw new IllegalStateException("Invalid getter method: " + m.getName());
    }

    /**
     * Get field name from Lambda method reference.
     *
     * @param methodRef method reference e.g. {@code User::getName}
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
     * Retrieves a map of property names to their corresponding metadata for the given class.
     * The method inspects the provided class using Java's introspection capabilities to
     * gather information about its properties, including their fields, getter, and setter methods.
     *
     * @param clazz the class to inspect for property metadata
     * @return a map where keys are property names and values are instances of PropertyMeta containing
     * detailed information about each property
     */
    public static Map<String, PropertyMeta> getBeanPropertyMetas(Class<?> clazz) {
        return BEAN_PROPERTY_CACHE.computeIfAbsent(clazz, c -> {
            try {
                Map<String, PropertyMeta> map = new HashMap<>();
                BeanInfo beanInfo = Introspector.getBeanInfo(c, Object.class);
                for (PropertyDescriptor p : beanInfo.getPropertyDescriptors()) {
                    String name = p.getName();
                    PropertyMeta pm = new PropertyMeta(name);
                    try {
                        // field is not required.
                        pm.setField(c.getDeclaredField(name));
                    } catch (NoSuchFieldException ignore) {
                        continue;
                    }
                    pm.setGetter(p.getReadMethod());
                    pm.setSetter(p.getWriteMethod());
                    map.put(name, pm);
                }
                return map;
            } catch (IntrospectionException e) {
                throw new IllegalStateException("Unable to introspect " + c.getName());
            }
        });
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
            @SuppressWarnings("unchecked") Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
            for (Constructor<T> constructor : constructors) {
                Class<?>[] paramClasses = constructor.getParameterTypes();
                if (paramClasses.length != constructorParameters.length) {
                    continue;
                }
                boolean match = true;
                for (int i = 0; i < paramClasses.length; i++) {
                    Class<?> paramClass = paramClasses[i];
                    Object value = constructorParameters[i];
                    if (value == null) {
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
                return constructor.newInstance(constructorParameters);
            }
            StringJoiner sb = new StringJoiner(", ");
            for (Object param : constructorParameters) {
                if (param == null) {
                    sb.add("null");
                    continue;
                }
                sb.add(param.getClass().getName());
            }
            throw new NoSuchMethodException(clazz.getName() + "<init>(" + sb + ")");
        }
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }
}
