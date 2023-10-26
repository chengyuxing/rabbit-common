package com.github.chengyuxing.common.utils;

import com.github.chengyuxing.common.DateTimes;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Function;

/**
 * 简单基本对象工具类
 */
public final class ObjectUtil {
    /**
     * 不限长赋值表达式<br>
     * 逻辑形如:
     * <blockquote>
     * <pre>
     *         if(a==b)
     *            return v1;
     *         else if(a==c)
     *            return v2;
     *         else if(a==d)
     *            return v3;
     *         # (可选)全部匹配不到的默认值
     *         else
     *            return v4;
     *     </pre>
     * </blockquote>
     *
     * @param value  值
     * @param equal  比较值
     * @param result 结果
     * @param more   更多
     * @return 结果
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
     * 从一个数组中返回第一个不为null的值，如果数组长度为0返回null
     *
     * @param values 一组值
     * @param <T>    类型参数
     * @return 不为null的值或全部为null
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
     * 获取一个对象或数组的值
     *
     * @param value 对象
     * @param key   键名或索引
     * @return 值
     * @throws IllegalArgumentException 如果javaBean字段访问异常
     */
    public static Object getValue(Object value, String key) {
        if (Objects.isNull(value)) {
            return null;
        }
        if (ReflectUtil.isBasicType(value)) {
            return null;
        }
        if (key.matches("\\d+")) {
            int idx = Integer.parseInt(key);
            if (value instanceof Collection) {
                int i = 0;
                for (Object v : (Collection<?>) value) {
                    if (i++ == idx) {
                        return v;
                    }
                }
            }
            if (value instanceof Object[]) {
                return ((Object[]) value)[idx];
            }
        }
        if (value instanceof Map) {
            //noinspection unchecked
            Map<String, ?> map = (Map<String, ?>) value;
            if (map.containsKey(key)) {
                return map.get(key);
            }
            return null;
        }
        Class<?> clazz = value.getClass();
        Method m = null;
        try {
            m = ReflectUtil.getGetMethod(clazz, key);
            if (!m.isAccessible()) {
                m.setAccessible(true);
            }
            return m.invoke(value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Invoke " + clazz.getName() + "#" + m.getName() + " error.", e);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No such field on " + value, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No such method on " + value, e);
        }
    }

    /**
     * 获取一个嵌套对象的值
     *
     * @param obj  深层嵌套的对象
     * @param path 路径表达式（{@code /a/b/0/name}）
     * @return 值
     * @throws IllegalArgumentException 如果调用目标javaBean错误
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
     * 获取一个嵌套对象的值
     *
     * @param obj            深层嵌套的对象
     * @param propertyChains 对象属性路径（{@code user.name}）
     * @return 值
     */
    public static Object getDeepValue(Object obj, String propertyChains) {
        if (propertyChains.contains(".")) {
            String path = '/' + propertyChains.replace('.', '/');
            return walkDeepValue(obj, path);
        }
        return getValue(obj, propertyChains);
    }

    /**
     * 将单个对象转换为对象数组
     *
     * @param obj 基本类型或集合或数组
     * @return 对象数组
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
     * 将Date转换为java8时间
     *
     * @param clazz java8时间的实现类
     * @param date  日期
     * @param <T>   类型参数
     * @return java8时间类型
     */
    @SuppressWarnings("unchecked")
    public static <T extends Temporal> T toTemporal(Class<?> clazz, Date date) {
        if (clazz == LocalDateTime.class) {
            return (T) date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        if (clazz == LocalDate.class) {
            return (T) date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (clazz == LocalTime.class) {
            return (T) date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        }
        if (clazz == Instant.class) {
            return (T) date.toInstant();
        }
        return null;
    }

    /**
     * 转换为整型
     *
     * @param obj 值
     * @return 整型
     */
    public static Integer toInteger(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        return Integer.parseInt(obj.toString());
    }

    /**
     * 转换为长整型
     *
     * @param obj 值
     * @return 长整型
     */
    public static Long toLong(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Long) {
            return (Long) obj;
        }
        return Long.parseLong(obj.toString());
    }

    /**
     * 转换为双精度型
     *
     * @param obj 值
     * @return 双精度型
     */
    public static Double toDouble(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Double) {
            return (Double) obj;
        }
        return Double.parseDouble(obj.toString());
    }

    /**
     * 转换为浮点型
     *
     * @param obj 值
     * @return 浮点型
     */
    public static Float toFloat(Object obj) {
        if (Objects.isNull(obj)) return null;
        if (obj instanceof Float) {
            return (Float) obj;
        }
        return Float.parseFloat(obj.toString());
    }

    /**
     * 实体转为map
     *
     * @param entity     标准java bean 实体
     * @param mapBuilder map构造器 [实体getter数，Map实现]
     * @param <T>        类型参数
     * @return map
     */
    public static <T extends Map<String, Object>> T entity2map(Object entity, Function<Integer, T> mapBuilder) {
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
                map.put(get.getName(), value);
            }
            return map;
        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
            throw new RuntimeException("convert to map error.", e);
        }
    }

    /**
     * map转为实体
     *
     * @param source                map
     * @param targetType            实体目标类型
     * @param constructorParameters 构造函数参数
     * @param <T>                   实体类型参数
     * @return 实体
     */
    public static <T> T map2entity(Map<String, Object> source, Class<T> targetType, Object... constructorParameters) {
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
                String field = set.getName();
                Object value = source.get(field);
                if (Objects.isNull(value) || setter.getParameterCount() != 1) {
                    continue;
                }
                // dataRow field type
                Class<?> dft = value.getClass();
                // entity field type
                Class<?> eft = setter.getParameterTypes()[0];
                if (eft.isAssignableFrom(dft)) {
                    setter.invoke(entity, value);
                    continue;
                }
                if (eft == String.class) {
                    setter.invoke(entity, value.toString());
                    continue;
                }
                if (eft == Character.class) {
                    setter.invoke(value.toString().charAt(0));
                    continue;
                }
                if (eft == Integer.class) {
                    setter.invoke(toInteger(value));
                    continue;
                }
                if (eft == Long.class) {
                    setter.invoke(toLong(value));
                    continue;
                }
                if (eft == Double.class) {
                    setter.invoke(toDouble(value));
                    continue;
                }
                if (eft == Float.class) {
                    setter.invoke(toFloat(value));
                    continue;
                }
                if (eft == Date.class) {
                    setter.invoke(entity, DateTimes.toDate(value.toString()));
                    continue;
                }
                if (Temporal.class.isAssignableFrom(eft)) {
                    if (Date.class.isAssignableFrom(dft)) {
                        setter.invoke(entity, toTemporal(eft, (Date) value));
                        continue;
                    }
                    if (dft == String.class) {
                        Date date = DateTimes.toDate(value.toString());
                        setter.invoke(entity, toTemporal(eft, date));
                    }
                    continue;
                }
                // map, collection or java bean.
                if (Map.class.isAssignableFrom(eft) || Collection.class.isAssignableFrom(eft) || !eft.getTypeName().startsWith("java.")) {
                    // maybe json string
                    if (dft == String.class) {
                        setter.invoke(entity, Jackson.toObject(value.toString(), eft));
                        continue;
                    }
                    // object array parsing to collection exclude blob
                    if (dft != byte[].class && value instanceof Object[]) {
                        if (List.class.isAssignableFrom(eft)) {
                            setter.invoke(entity, new ArrayList<>(Arrays.asList((Object[]) value)));
                            continue;
                        }
                        if (Set.class.isAssignableFrom(eft)) {
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
}
