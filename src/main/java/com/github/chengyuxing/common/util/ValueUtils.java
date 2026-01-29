package com.github.chengyuxing.common.util;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.PropertyMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Value util.
 */
public final class ValueUtils {
    public static final Pattern VAR_PATH_EXPRESSION_PATTERN = Pattern.compile("[a-zA-Z_]\\w*(\\.\\w+|\\[\\d+])*");
    private static final Map<Class<?>, Function<@NotNull Object, @Nullable Object>> VALUE_ADAPTORS = new HashMap<>();

    static {
        VALUE_ADAPTORS.put(String.class, Object::toString);
        VALUE_ADAPTORS.put(Character.class, o -> {
            String s = o.toString();
            if (s.length() != 1) {
                throw new IllegalArgumentException("Cannot convert to char: " + s);
            }
            return s.charAt(0);
        });
        VALUE_ADAPTORS.put(char.class, VALUE_ADAPTORS.get(Character.class));
        VALUE_ADAPTORS.put(Integer.class, ValueUtils::toInteger);
        VALUE_ADAPTORS.put(int.class, ValueUtils::toInteger);
        VALUE_ADAPTORS.put(Double.class, ValueUtils::toDouble);
        VALUE_ADAPTORS.put(double.class, ValueUtils::toDouble);
        VALUE_ADAPTORS.put(Long.class, ValueUtils::toLong);
        VALUE_ADAPTORS.put(long.class, ValueUtils::toLong);
        VALUE_ADAPTORS.put(Float.class, ValueUtils::toFloat);
        VALUE_ADAPTORS.put(float.class, ValueUtils::toFloat);
    }

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
        if (Objects.equals(value, equal)) {
            return result;
        }
        for (int i = 0; i + 1 < more.length; i += 2) {
            if (Objects.equals(value, more[i])) {
                return more[i + 1];
            }
        }
        return (more.length & 1) == 1
                ? more[more.length - 1]
                : null;
    }

    /**
     * Returns the first non-null value from the given array of values.
     *
     * @param <T>    the type of the values
     * @param values an array of values to be checked for the first non-null value
     * @return the first non-null value in the array, or null if all values are null
     */
    @SafeVarargs
    public static @Nullable <T> T coalesce(T @NotNull ... values) {
        for (T v : values) {
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    /**
     * Returns the first non-null value from the given array of values.
     * If all values are null, a NullPointerException is thrown.
     *
     * @param <T>    the type of the values
     * @param values an array of values to check for the first non-null element
     * @return the first non-null value in the array
     * @throws NullPointerException if all provided values are null
     */
    @SafeVarargs
    public static @NotNull <T> T coalesceNonNull(T @NotNull ... values) {
        for (T v : values) {
            if (v != null) {
                return v;
            }
        }
        throw new NullPointerException("All values are null");
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
    public static @Nullable Object accessValue(Object obj, @NotNull String key) {
        if (obj == null) {
            return null;
        }
        if (StringUtils.isNonNegativeInteger(key)) {
            int index = Integer.parseInt(key);
            if (obj.getClass().isArray()) {
                return Array.get(obj, index);
            }
            if (obj instanceof List<?>) {
                return ((List<?>) obj).get(index);
            }
            if (obj instanceof Iterable<?>) {
                int i = 0;
                for (Object e : ((Iterable<?>) obj)) {
                    if (i++ == index) {
                        return e;
                    }
                }
                throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + (i + 1) + " on " + obj);
            }
        }
        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).get(key);
        }
        Class<?> clazz = obj.getClass();
        PropertyMeta meta = ReflectUtils.getBeanPropertyMetas(clazz).get(key);
        if (meta != null && meta.getGetter() != null) {
            try {
                return meta.getGetter().invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to invoke getter '" + key + "' on " + clazz.getName(), e);
            }
        }
        throw new NoSuchElementException("No readable property '" + key + "' on " + clazz.getName());
    }

    /**
     * Retrieves a value from a nested object structure based on the provided key list.
     * <p>
     * Access the value by index if key is number and the value is a collection or array,
     * otherwise by property name.
     *
     * @param obj  The object to search within. Can be a collection, array, map, or any object.
     * @param keys the key list to access the value
     * @return The value found at the specified key, or null if the key is invalid, the object is null,
     * or the value does not exist at the given key.
     */
    public static @Nullable Object accessDeepValue(Object obj, @NotNull List<String> keys) {
        if (obj == null) return null;
        if (keys.isEmpty()) return obj;
        Object result = obj;
        for (String key : keys) {
            if (key == null) throw new IllegalArgumentException("key is null");
            result = accessValue(result, key);
            if (result == null) return null;
        }
        return result;
    }

    /**
     * Retrieves a value from a nested object structure based on the provided path.
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
        if (obj == null) {
            return null;
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path expression syntax error, must startsWith '/', for example '/" + path + "'");
        }
        String[] keys = path.substring(1).split("/");
        return accessDeepValue(obj, Arrays.asList(keys));
    }

    /**
     * Retrieves a value from a nested object structure based on the provided property chains.
     *
     * @param obj            the object to search within. Can be a collection, array, map, or any object.
     * @param propertyChains the dot-separated property chains used to access the value. For example, "{@code user.name}" would access the "{@code name}" property of the "{@code user}" object.
     * @return the value found at the specified property chains, or null if the property chains are invalid, the object is null, or the value does not exist at the given path.
     */
    public static @Nullable Object getDeepValue(Object obj, @NotNull String propertyChains) {
        if (VAR_PATH_EXPRESSION_PATTERN.matcher(propertyChains).matches()) {
            return accessDeepValue(obj, decodeKeyPathExpression(propertyChains));
        }
        throw new IllegalArgumentException("Property chains '" + propertyChains + "' is invalid");
    }

    /**
     * Decode var key path expression to key list.
     *
     * @param keyPath key path expression
     * @return key list
     */
    public static @NotNull @Unmodifiable List<String> decodeKeyPathExpression(String keyPath) {
        String[] keys = keyPath.replaceAll("\\[(\\d+)]", ".$1").split("\\.");
        return Arrays.asList(keys);
    }

    /**
     * Converts the given object into an Iterable.
     *
     * @param obj the object to be converted into an Iterable. This can be null, an instance of Iterable, an array, or any other object.
     * @return an Iterable representation of the input object. Returns an empty list if the input is null, casts the object to Iterable if it is one, wraps the object in a singleton if it is not
     * an Iterable or an array, and provides an Iterable view backed by the array if the object is an array.
     */
    public static Iterable<?> asIterable(Object obj) {
        if (obj == null) {
            return Collections.emptyList();
        }
        if (obj instanceof Iterable<?>) {
            return (Iterable<?>) obj;
        }
        if (obj.getClass().isArray()) {
            return () -> new Iterator<Object>() {
                int i = 0;
                final int len = Array.getLength(obj);

                @Override
                public boolean hasNext() {
                    return i < len;
                }

                @Override
                public Object next() {
                    if (i >= len) {
                        throw new NoSuchElementException();
                    }
                    return Array.get(obj, i++);
                }
            };
        }
        return Collections.singletonList(obj);
    }

    /**
     * Converts a given Date object to a specified Temporal type.
     *
     * @param clazz  The class of the target Temporal type to which the Date should be converted. Supported types include LocalDateTime, ZonedDateTime, OffsetDateTime, LocalDate, LocalTime
     *               , OffsetTime, and Instant.
     * @param date   The Date object to convert.
     * @param zoneId The ZoneId representing the time zone to use for the conversion.
     * @return An instance of the specified Temporal type.
     * @throws IllegalArgumentException If the provided class is not one of the supported Temporal types.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Temporal> T toTemporal(Class<T> clazz, Date date, ZoneId zoneId) {
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
        throw new IllegalArgumentException("Cannot convert " + date.getClass() + " to " + clazz);
    }

    /**
     * Convert date to java8 temporal.
     *
     * @param clazz java8 temporal implementation
     * @param date  date
     * @param <T>   result type
     * @return java8 temporal implementation
     */
    public static <T extends Temporal> T toTemporal(Class<T> clazz, Date date) {
        return toTemporal(clazz, date, ZoneId.systemDefault());
    }

    public static @Nullable Integer toInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        return Integer.parseInt(obj.toString());
    }

    public static @Nullable Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) {
            return (Long) obj;
        }
        return Long.parseLong(obj.toString());
    }

    public static @Nullable Double toDouble(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Double) {
            return (Double) obj;
        }
        return Double.parseDouble(obj.toString());
    }

    public static @Nullable Float toFloat(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Float) {
            return (Float) obj;
        }
        return Float.parseFloat(obj.toString());
    }

    /**
     * Adapts the given value to the specified target type.
     *
     * @param targetType The target type to which the value should be adapted. Must not be null.
     * @param value      The value to adapt. Can be null.
     * @param <T>        The type of the target.
     * @return The adapted value, or null if the input value is null and no conversion can be applied.
     * @throws IllegalArgumentException If the conversion from the value's type to the target type is unsupported.
     */
    @SuppressWarnings("unchecked")
    public static @Nullable <T> T adaptValue(@NotNull Class<T> targetType, Object value) {
        if (value == null) {
            return null;
        }
        Class<?> valueType = value.getClass();
        if (targetType.isAssignableFrom(valueType)) {
            return (T) value;
        }
        Function<Object, Object> f = VALUE_ADAPTORS.get(targetType);
        if (f != null) {
            return (T) f.apply(value);
        }
        if (Date.class.isAssignableFrom(targetType)) {
            return (T) MostDateTime.toDate(value.toString());
        }
        if (Temporal.class.isAssignableFrom(targetType)) {
            if (Date.class.isAssignableFrom(valueType)) {
                return (T) toTemporal((Class<? extends Temporal>) targetType, (Date) value);
            }
            if (valueType == String.class) {
                return (T) toTemporal((Class<? extends Temporal>) targetType, MostDateTime.toDate(value.toString()));
            }
            if (valueType == Long.class || value == long.class) {
                return (T) toTemporal((Class<? extends Temporal>) targetType, MostDateTime.of((long) value).toDate());
            }
        }
        // array to collection
        if (Collection.class.isAssignableFrom(targetType)) {
            // object array parsing to collection exclude blob
            if (valueType != byte[].class) {
                Iterable<?> it = asIterable(value);
                if (List.class.isAssignableFrom(targetType)) {
                    List<Object> list = new ArrayList<>();
                    it.forEach(list::add);
                    return (T) list;
                }
                if (Set.class.isAssignableFrom(targetType)) {
                    Set<Object> set = new HashSet<>();
                    it.forEach(set::add);
                    return (T) set;
                }
            }
        }
        throw new IllegalArgumentException("unsupported conversion from " + value.getClass().getName() + " to " + targetType.getName() + ", value=" + value);
    }

    /**
     * Multi key-value pairs convert to map.
     *
     * @param mapBuilder (key-value count) {@code ->} (new Map instance)
     * @param input      multi key-value pairs data
     * @param <T>        result type
     * @return map
     */
    public static <T extends Map<String, Object>> T pairsToMap(@NotNull Function<Integer, T> mapBuilder, Object @NotNull ... input) {
        if ((input.length & 1) == 1) {
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
        if (entity == null) return mapBuilder.apply(0);
        try {
            Class<?> clazz = entity.getClass();
            Map<String, PropertyMeta> metas = ReflectUtils.getBeanPropertyMetas(clazz);
            T map = mapBuilder.apply(metas.size());
            for (Map.Entry<String, PropertyMeta> e : metas.entrySet()) {
                PropertyMeta meta = e.getValue();
                Method getter = meta.getGetter();
                if (getter == null) {
                    continue;
                }

                String name = meta.getField() == null
                        ? e.getKey()
                        : fieldMapper.apply(meta.getField());

                Object value = getter.invoke(entity);

                map.put(name, value);
            }
            return map;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("convert to map error", e);
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
     * @param valueAdaptor          invoke setter to set the mapping value (entity field, map value) -&gt; (new value)
     * @param constructorParameters constructor parameters
     * @param <T>                   entity type
     * @return entity
     */
    public static <T> @Nullable T mapToEntity(Map<String, Object> source, @NotNull Class<T> targetType, @NotNull Function<Field, String> fieldMapper, @Nullable BiFunction<Field, Object, Object> valueAdaptor, Object... constructorParameters) {
        try {
            if (source == null) return null;
            T entity = ReflectUtils.getInstance(targetType, constructorParameters);
            if (source.isEmpty()) return entity;
            Map<String, PropertyMeta> metas = ReflectUtils.getBeanPropertyMetas(targetType);
            for (Map.Entry<String, PropertyMeta> e : metas.entrySet()) {
                PropertyMeta meta = e.getValue();
                Method setter = meta.getSetter();
                if (setter == null) {
                    continue;
                }

                boolean hasField = meta.getField() != null;

                String name = hasField
                        ? fieldMapper.apply(meta.getField())
                        : e.getKey();

                Object value = hasField && valueAdaptor != null
                        ? valueAdaptor.apply(meta.getField(), source.get(name))
                        : adaptValue(setter.getParameterTypes()[0], source.get(name));

                setter.invoke(entity, value);
            }
            return entity;
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new IllegalStateException("convert to " + targetType.getTypeName() + " error", e);
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
