package com.github.chengyuxing.common.utils;

import com.github.chengyuxing.common.MostDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Object util.
 */
public final class ObjectUtil {
    /**
     * Decodes the given value by comparing it with a series of equal-value pairs and returns the corresponding result.
     *
     * @param value  The value to decode.
     * @param equal  The first comparison value.
     * @param result The result to return if the value matches the first comparison value.
     * @param more   Additional pairs of equal-value and result. These must be provided in pairs, where the first
     *               element is the comparison value and the second is the result to return on a match.
     * @return The decoded value matching the input value, or the last provided result if no match is found.
     */
    public static @Nullable Object decode(Object value, Object equal, Object result, Object... more) {
        Object[] objs = new Object[more.length + 2];
        objs[0] = equal;
        objs[1] = result;
        System.arraycopy(more, 0, objs, 2, more.length);

        Object res = null;
        int i = 0;
        while (i < objs.length) {
            if (Objects.equals(value, objs[i])) {
                res = objs[i + 1];
                break;
            }
            if (i == objs.length - 1) {
                res = objs[i];
                break;
            }
            i += 2;
        }
        return res;
    }

    /**
     * Returns the first non-null value from the given array of values.
     *
     * @param <T>    the type of the values
     * @param values an array of values to be checked for the first non-null value
     * @return the first non-null value in the array, or null if all values are null
     */
    @SafeVarargs
    public static @Nullable <T> T coalesce(T... values) {
        for (T v : values) {
            if (Objects.nonNull(v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Compares two objects and returns null if they are equal, otherwise returns the first object.
     *
     * @param a the first object to compare
     * @param b the second object to compare
     * @return the first object if it is not equal to the second, or null if both are equal
     */
    public static @Nullable Object nullif(Object a, Object b) {
        if (Objects.equals(a, b)) {
            return null;
        }
        return a;
    }

    /**
     * Retrieves the value associated with the specified key from the given object.
     * The method supports accessing values in collections, arrays, and maps using an index or key.
     * It also supports invoking getter methods on objects to retrieve property values.
     *
     * @param obj The object from which to retrieve the value. Can be a collection, array, map, or any object.
     * @param key The key or index used to access the value. If {@code obj} is a collection or array, {@code key} should be a numeric string representing the index.
     *            For maps, it should be the key as a string. For objects, it should be the name of the getter method (e.g., "{@code name}" for a method named "{@code getName}").
     * @return The value associated with the key, or null if the key does not exist, the object is null, or the object is of a basic type.
     * @throws IllegalArgumentException If the index is out of bounds for a collection or array, or if there is no corresponding getter method or field for an object.
     */
    public static @Nullable Object getValue(Object obj, @NotNull String key) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (ReflectUtil.isBasicType(obj)) {
            return null;
        }
        if (key.matches("\\d+")) {
            int index = Integer.parseInt(key);
            if (obj instanceof Collection || obj instanceof Object[]) {
                Object[] arr = toArray(obj);
                if (index < 0 || index >= arr.length) {
                    throw new IndexOutOfBoundsException("Index out of bounds " + index + " on " + obj);
                }
                return arr[index];
            }
        }
        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).get(key);
        }
        Class<?> clazz = obj.getClass();
        Method m = null;
        try {
            m = ReflectUtil.getGetMethod(clazz, key);
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            return m.invoke(obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Invoke " + clazz.getName() + "#" + m.getName() + " error.", e);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No such field '" + key + "' on " + obj, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No such getter method of '" + key + "' on " + obj, e);
        }
    }

    /**
     * Recursively retrieves a value from a nested object structure based on the provided path.
     *
     * @param obj  The object to search within. Can be a collection, array, map, or any object.
     * @param path The path expression to the desired value, starting with a '{@code /}'. Each segment of the path
     *             should be separated by a '{@code /}'. For example, "{@code /user/name}" would access the "name" property
     *             of the "user" object.
     * @return The value found at the specified path, or null if the path is invalid, the object is null,
     * or the value does not exist at the given path.
     * @throws IllegalArgumentException If the path expression syntax is incorrect (does not start with a '{@code /}').
     */
    public static @Nullable Object walkDeepValue(Object obj, @NotNull String path) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path expression syntax error, must startsWith '/', for example '/" + path + "'");
        }
        String paths = path.substring(1);
        int pathIndex = paths.indexOf("/");
        if (pathIndex == -1) {
            return getValue(obj, paths);
        }
        String key = paths.substring(0, pathIndex);
        String tail = paths.substring(pathIndex);
        return walkDeepValue(getValue(obj, key), tail);
    }

    /**
     * Retrieves a value from a nested object structure based on the provided property chains.
     *
     * @param obj            the object to search within. Can be a collection, array, map, or any object.
     * @param propertyChains the dot-separated property chains used to access the value. For example, "{@code user.name}" would access the "{@code name}" property of the "{@code user}" object.
     * @return the value found at the specified property chains, or null if the property chains are invalid, the object is null, or the value does not exist at the given path.
     */
    public static @Nullable Object getDeepValue(Object obj, @NotNull String propertyChains) {
        if (propertyChains.contains(".")) {
            String path = '/' + propertyChains.replace('.', '/');
            return walkDeepValue(obj, path);
        }
        return getValue(obj, propertyChains);
    }

    /**
     * Converts the given object into an array.
     * If the object is null, returns an empty array.
     * If the object is already an array, it casts and returns it.
     * If the object is a Collection, it converts the collection to an array.
     * Otherwise, it wraps the object in an array.
     *
     * @param obj the object to be converted into an array
     * @return an Object array representing the input object
     */
    @SuppressWarnings("unchecked")
    public static Object[] toArray(Object obj) {
        if (Objects.isNull(obj)) {
            return new Object[0];
        }
        Object[] values;
        if (obj instanceof Object[]) {
            values = (Object[]) obj;
        } else if (obj instanceof Collection) {
            values = ((Collection<Object>) obj).toArray();
        } else {
            values = new Object[]{obj};
        }
        return values;
    }

    /**
     * Convert date to java8 temporal.
     *
     * @param clazz  java8 temporal implementation
     * @param date   date
     * @param zoneId zone id
     * @param <T>    result type
     * @return java8 temporal implementation
     */
    @SuppressWarnings("unchecked")
    public static @Nullable <T extends Temporal> T toTemporal(Class<T> clazz, Date date, ZoneId zoneId) {
        if (clazz == LocalDateTime.class) {
            return (T) date.toInstant().atZone(zoneId).toLocalDateTime();
        }
        if (clazz == ZonedDateTime.class) {
            return (T) date.toInstant().atZone(zoneId);
        }
        if (clazz == OffsetDateTime.class) {
            return (T) date.toInstant().atZone(zoneId).toOffsetDateTime();
        }
        if (clazz == LocalDate.class) {
            return (T) date.toInstant().atZone(zoneId).toLocalDate();
        }
        if (clazz == LocalTime.class) {
            return (T) date.toInstant().atZone(zoneId).toLocalTime();
        }
        if (clazz == OffsetTime.class) {
            return (T) date.toInstant().atZone(zoneId).toOffsetDateTime().toOffsetTime();
        }
        if (clazz == Instant.class) {
            return (T) date.toInstant();
        }
        return null;
    }

    /**
     * Convert date to java8 temporal.
     *
     * @param clazz java8 temporal implementation
     * @param date  date
     * @param <T>   result type
     * @return java8 temporal implementation
     */
    public static @Nullable <T extends Temporal> T toTemporal(Class<T> clazz, Date date) {
        return toTemporal(clazz, date, ZoneId.systemDefault());
    }

    public static @Nullable Integer toInteger(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        return Integer.parseInt(obj.toString());
    }

    public static @Nullable Long toLong(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Long) {
            return (Long) obj;
        }
        return Long.parseLong(obj.toString());
    }

    public static @Nullable Double toDouble(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Double) {
            return (Double) obj;
        }
        return Double.parseDouble(obj.toString());
    }

    public static @Nullable Float toFloat(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Float) {
            return (Float) obj;
        }
        return Float.parseFloat(obj.toString());
    }

    /**
     * Converts the given value to the specified target type.
     *
     * @param targetType The class of the type to which the value should be converted.
     * @param value      The value to convert. If null, the method returns null.
     * @return The converted value in the target type, or the original value if conversion is not supported.
     */
    public static Object convertValue(Class<?> targetType, Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        Class<?> vt = value.getClass();
        if (targetType.isAssignableFrom(vt)) {
            return value;
        }
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == Character.class || targetType == char.class) {
            return value.toString().charAt(0);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return toInteger(value);
        }
        if (targetType == Long.class || targetType == long.class) {
            return toLong(value);
        }
        if (targetType == Double.class || targetType == double.class) {
            return toDouble(value);
        }
        if (targetType == Float.class || targetType == float.class) {
            return toFloat(value);
        }
        if (targetType == Date.class) {
            return MostDateTime.toDate(value.toString());
        }
        if (Temporal.class.isAssignableFrom(targetType)) {
            @SuppressWarnings("unchecked") Class<? extends Temporal> j8DateTypeClass = (Class<? extends Temporal>) targetType;
            if (Date.class.isAssignableFrom(vt)) {
                return toTemporal(j8DateTypeClass, (Date) value);
            }
            if (vt == String.class) {
                Date date = MostDateTime.toDate(value.toString());
                return toTemporal(j8DateTypeClass, date);
            }
        }
        // array to collection
        if (Collection.class.isAssignableFrom(targetType)) {
            // object array parsing to collection exclude blob
            if (vt != byte[].class && value instanceof Object[]) {
                if (List.class.isAssignableFrom(targetType)) {
                    return new ArrayList<>(Arrays.asList((Object[]) value));
                }
                if (Set.class.isAssignableFrom(targetType)) {
                    return new HashSet<>(Arrays.asList((Object[]) value));
                }
            }
        }
        return value;
    }

    /**
     * Multi key-value pairs convert to map.
     *
     * @param mapBuilder (key-value count) {@code ->} (new Map instance)
     * @param input      multi key-value pairs data
     * @param <T>        result type
     * @return map
     */
    public static <T extends Map<String, Object>> T pairsToMap(@NotNull Function<Integer, T> mapBuilder, Object... input) {
        if ((input.length & 1) != 0) {
            throw new IllegalArgumentException("key value are not a pair.");
        }
        int capacity = input.length >> 1;
        T map = mapBuilder.apply(capacity);
        for (int i = 0; i < capacity; i++) {
            int idx = i << 1;
            map.put(input[idx].toString(), input[idx + 1]);
        }
        return map;
    }

    /**
     * Entity convert to map.
     *
     * @param entity      standard java bean entity
     * @param fieldMapper getter field mapping to the result map key
     * @param mapBuilder  (key-value count) {@code ->} (new Map instance)
     * @param <T>         result type
     * @return map
     */
    public static <T extends Map<String, Object>> T entityToMap(Object entity, @NotNull Function<Field, String> fieldMapper, @NotNull Function<Integer, T> mapBuilder) {
        if (Objects.isNull(entity)) return mapBuilder.apply(0);
        try {
            Class<?> clazz = entity.getClass();
            List<Method> getters = ReflectUtil.getRWMethods(entity.getClass()).getItem1();
            T map = mapBuilder.apply(getters.size());
            for (Method getter : getters) {
                Field get;
                try {
                    get = ReflectUtil.getGetterField(clazz, getter);
                } catch (NoSuchFieldException e) {
                    continue;
                }
                if (Objects.isNull(get)) {
                    continue;
                }
                Object value = getter.invoke(entity);
                String name = fieldMapper.apply(get);
                map.put(name, value);
            }
            return map;
        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
            throw new RuntimeException("convert to map error.", e);
        }
    }

    /**
     * Entity convert to map.
     *
     * @param entity     standard java bean entity
     * @param mapBuilder (key-value count) {@code ->} (new Map instance)
     * @param <T>        result type
     * @return map
     */
    public static <T extends Map<String, Object>> T entityToMap(Object entity, Function<Integer, T> mapBuilder) {
        return entityToMap(entity, Field::getName, mapBuilder);
    }

    /**
     * Map convert to entity.
     *
     * @param source                map
     * @param targetType            entity class
     * @param fieldMapper           setter field mapping to the source map key
     * @param valueMapper           invoke setter to set the mapping value (entity field, map value) -&gt; (new value)
     * @param constructorParameters constructor parameters
     * @param <T>                   entity type
     * @return entity
     */
    public static <T> T mapToEntity(Map<String, Object> source, @NotNull Class<T> targetType, @NotNull Function<Field, String> fieldMapper, @Nullable BiFunction<Field, Object, Object> valueMapper, Object... constructorParameters) {
        try {
            if (Objects.isNull(source)) return null;
            T entity = ReflectUtil.getInstance(targetType, constructorParameters);
            if (source.isEmpty()) return entity;
            for (Method setter : ReflectUtil.getRWMethods(targetType).getItem2()) {
                Field set;
                try {
                    set = ReflectUtil.getSetterField(targetType, setter);
                } catch (NoSuchFieldException e) {
                    continue;
                }
                if (Objects.isNull(set)) {
                    continue;
                }
                if (setter.getParameterCount() != 1) {
                    continue;
                }

                String name = fieldMapper.apply(set);
                Object value = source.get(name);

                if (Objects.isNull(value)) {
                    setter.invoke(entity, (Object) null);
                    continue;
                }
                if (Objects.nonNull(valueMapper)) {
                    Object mapperValue = valueMapper.apply(set, value);
                    setter.invoke(entity, mapperValue);
                    continue;
                }
                setter.invoke(entity, convertValue(setter.getParameterTypes()[0], value));
            }
            return entity;
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IntrospectionException |
                 IllegalAccessException e) {
            throw new RuntimeException("convert to " + targetType.getTypeName() + " error.", e);
        }
    }

    /**
     * Map convert to entity.
     *
     * @param source                map
     * @param targetType            entity class
     * @param constructorParameters constructor parameters
     * @param <T>                   entity type
     * @return entity
     */
    public static <T> T mapToEntity(Map<String, Object> source, @NotNull Class<T> targetType, Object... constructorParameters) {
        return mapToEntity(source, targetType, Field::getName, null, constructorParameters);
    }
}
