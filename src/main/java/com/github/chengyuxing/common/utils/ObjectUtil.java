package com.github.chengyuxing.common.utils;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.TiFunction;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Function;

/**
 * Object util.
 */
public final class ObjectUtil {
    /**
     * Decode values,
     * logic following:
     * <blockquote>
     * <pre>
     *         if(a == b)
     *            return v1;
     *         else if(a == c)
     *            return v2;
     *         else if(a == d)
     *            return v3;
     *         # optional default value.
     *         else
     *            return v4;
     *     </pre>
     * </blockquote>
     *
     * @param value  value
     * @param equal  compare target value
     * @param result equal result
     * @param more   more above.
     * @return result
     */
    public static Object decode(Object value, Object equal, Object result, Object... more) {
        Object[] objs = new Object[more.length + 2];
        objs[0] = equal;
        objs[1] = result;
        System.arraycopy(more, 0, objs, 2, more.length);

        Object res = null;
        int i = 0;
        while (i < objs.length) {
            if (value.equals(objs[i])) {
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
     * Get a value until not null.
     *
     * @param values values
     * @param <T>    value type
     * @return value or null
     */
    @SafeVarargs
    public static <T> T coalesce(T... values) {
        for (T v : values) {
            if (Objects.nonNull(v)) {
                return v;
            }
        }
        return null;
    }

    /**
     * Get value of object.
     *
     * @param obj object or array
     * @param key key or index
     * @return value
     * @throws IllegalArgumentException if java bean access error
     */
    public static Object getValue(Object obj, String key) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (ReflectUtil.isBasicType(obj)) {
            return null;
        }
        if (key.matches("\\d+")) {
            int idx = Integer.parseInt(key);
            if (obj instanceof Collection) {
                int i = 0;
                for (Object v : (Collection<?>) obj) {
                    if (i++ == idx) {
                        return v;
                    }
                }
            }
            if (obj instanceof Object[]) {
                return ((Object[]) obj)[idx];
            }
        }
        if (obj instanceof Map) {
            //noinspection unchecked
            Map<String, ?> map = (Map<String, ?>) obj;
            if (map.containsKey(key)) {
                return map.get(key);
            }
            return null;
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
            throw new IllegalArgumentException("No such field on " + obj, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No such method on " + obj, e);
        }
    }

    /**
     * Get value of nested object.
     *
     * @param obj  nested object.
     * @param path path expression（{@code /a/b/0/name}）
     * @return value
     * @throws IllegalArgumentException if java bean access error
     */
    public static Object walkDeepValue(Object obj, String path) {
        if (Objects.isNull(obj)) {
            return null;
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path expression syntax error, must startsWith '/', for example '/" + path + "'");
        }
        String trimStart = path.substring(1);
        if (!trimStart.contains("/")) {
            return getValue(obj, trimStart);
        }
        String key = trimStart.substring(0, trimStart.indexOf("/"));
        String tail = trimStart.substring(trimStart.indexOf("/"));
        return walkDeepValue(getValue(obj, key), tail);
    }

    /**
     * Get value of nested object.
     *
     * @param obj            nested object
     * @param propertyChains property chains（{@code user.name}）
     * @return value
     */
    public static Object getDeepValue(Object obj, String propertyChains) {
        if (propertyChains.contains(".")) {
            String path = '/' + propertyChains.replace('.', '/');
            return walkDeepValue(obj, path);
        }
        return getValue(obj, propertyChains);
    }

    /**
     * Convert collection to array.
     *
     * @param obj boxed type array or collection
     * @return object array
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
    public static <T extends Temporal> T toTemporal(Class<T> clazz, Date date) {
        return toTemporal(clazz, date, ZoneId.systemDefault());
    }

    public static Integer toInteger(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        return Integer.parseInt(obj.toString());
    }

    public static Long toLong(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Long) {
            return (Long) obj;
        }
        return Long.parseLong(obj.toString());
    }

    public static Double toDouble(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Double) {
            return (Double) obj;
        }
        return Double.parseDouble(obj.toString());
    }

    public static Float toFloat(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Float) {
            return (Float) obj;
        }
        return Float.parseFloat(obj.toString());
    }

    /**
     * Multi key-value pairs convert to map.
     *
     * @param mapBuilder (key-value count) {@code ->} (new Map instance)
     * @param input      multi key-value pairs data
     * @param <T>        result type
     * @return map
     */
    public static <T extends Map<String, Object>> T pairs2map(Function<Integer, T> mapBuilder, Object... input) {
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
    public static <T extends Map<String, Object>> T entity2map(Object entity, Function<Field, String> fieldMapper, Function<Integer, T> mapBuilder) {
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
    public static <T extends Map<String, Object>> T entity2map(Object entity, Function<Integer, T> mapBuilder) {
        return entity2map(entity, Field::getName, mapBuilder);
    }

    /**
     * Map convert to entity.
     *
     * @param source                map
     * @param targetType            entity class
     * @param fieldMapper           setter field mapping to the source map key
     * @param valueMapper           invoke setter to set the mapping value (value type, entity field type, value) -&gt; (new value)
     * @param constructorParameters constructor parameters
     * @param <T>                   entity type
     * @return entity
     */
    public static <T> T map2entity(Map<String, Object> source, Class<T> targetType, Function<Field, String> fieldMapper, TiFunction<Class<?>, Class<?>, Object, Object> valueMapper, Object... constructorParameters) {
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
                String name = fieldMapper.apply(set);
                Object value = source.get(name);
                if (Objects.isNull(value) || setter.getParameterCount() != 1) {
                    continue;
                }
                // dataRow field type
                Class<?> vt = value.getClass();
                // entity field type
                Class<?> et = setter.getParameterTypes()[0];

                if (Objects.nonNull(valueMapper)) {
                    Object mapperValue = valueMapper.apply(vt, et, value);
                    if (Objects.nonNull(mapperValue)) {
                        setter.invoke(entity, mapperValue);
                        continue;
                    }
                }

                if (et.isAssignableFrom(vt)) {
                    setter.invoke(entity, value);
                    continue;
                }
                if (et == String.class) {
                    setter.invoke(entity, value.toString());
                    continue;
                }
                if (et == Character.class || et == char.class) {
                    setter.invoke(entity, value.toString().charAt(0));
                    continue;
                }
                if (et == Integer.class || et == int.class) {
                    setter.invoke(entity, toInteger(value));
                    continue;
                }
                if (et == Long.class || et == long.class) {
                    setter.invoke(entity, toLong(value));
                    continue;
                }
                if (et == Double.class || et == double.class) {
                    setter.invoke(entity, toDouble(value));
                    continue;
                }
                if (et == Float.class || et == float.class) {
                    setter.invoke(entity, toFloat(value));
                    continue;
                }
                if (et == Date.class) {
                    setter.invoke(entity, MostDateTime.toDate(value.toString()));
                    continue;
                }
                if (Temporal.class.isAssignableFrom(et)) {
                    @SuppressWarnings("unchecked") Class<? extends Temporal> j8DateTypeClass = (Class<? extends Temporal>) et;
                    if (Date.class.isAssignableFrom(vt)) {
                        setter.invoke(entity, toTemporal(j8DateTypeClass, (Date) value));
                        continue;
                    }
                    if (vt == String.class) {
                        Date date = MostDateTime.toDate(value.toString());
                        setter.invoke(entity, toTemporal(j8DateTypeClass, date));
                    }
                    continue;
                }
                // array to collection
                if (Collection.class.isAssignableFrom(et)) {
                    // object array parsing to collection exclude blob
                    if (vt != byte[].class && value instanceof Object[]) {
                        if (List.class.isAssignableFrom(et)) {
                            setter.invoke(entity, new ArrayList<>(Arrays.asList((Object[]) value)));
                            continue;
                        }
                        if (Set.class.isAssignableFrom(et)) {
                            setter.invoke(entity, new HashSet<>(Arrays.asList((Object[]) value)));
                        }
                    }
                }
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
    public static <T> T map2entity(Map<String, Object> source, Class<T> targetType, Object... constructorParameters) {
        return map2entity(source, targetType, Field::getName, null, constructorParameters);
    }
}
