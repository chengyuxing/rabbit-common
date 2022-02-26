package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.ReflectUtil;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.chengyuxing.common.utils.ReflectUtil.json2Obj;
import static com.github.chengyuxing.common.utils.ReflectUtil.obj2Json;

/**
 * 不可变行数据类型
 */
public final class DataRow {
    private final String[] names;
    private final Object[] values;

    DataRow(String[] names, Object[] values) {
        this.names = names;
        this.values = values;
    }

    /**
     * 新建一个DataRow
     *
     * @param names  字段
     * @param values 值
     * @return 新实例
     */
    public static DataRow of(String[] names, Object[] values) {
        if (names.length == values.length) {
            return new DataRow(names, values);
        }
        throw new IllegalArgumentException("all of 3 args length not equal!");
    }

    /**
     * @return 一个空的DataRow
     */
    public static DataRow empty() {
        return of(new String[0], new Object[0]);
    }

    /**
     * 如果有元素则判断为非空
     *
     * @return 是否为非空
     */
    public boolean nonEmpty() {
        return names.length != 0;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public List<Object> getValues() {
        return Arrays.asList(values);
    }

    /**
     * 获取字段
     *
     * @return 字段
     */
    public List<String> getNames() {
        return Arrays.asList(names);
    }

    /**
     * 根据字段名获取类型
     *
     * @param name 字段
     * @return 如果值存在或不为null返回值类型名称，否则返回null
     */
    public String getType(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getType(index);
    }

    /**
     * 获取值类型
     *
     * @param index 索引
     * @return 如果值不为null返回值类型名称，否则返回null
     */
    public String getType(int index) {
        Object v = get(index);
        if (v == null) {
            return null;
        }
        return v.getClass().getName();
    }

    /**
     * 获取值
     *
     * @param index 索引
     * @param <T>   类型参数
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        return (T) values[index];
    }

    /**
     * 获取可空值
     *
     * @param index 索引
     * @param <T>   类型参数
     * @return 可空值
     */
    public <T> Optional<T> getOptional(int index) {
        return Optional.ofNullable(get(index));
    }

    /**
     * 获取值
     *
     * @param name 名称
     * @param <T>  类型参数
     * @return 值
     */
    public <T> T get(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return get(index);
    }

    /**
     * 获取可空值
     *
     * @param name 名字
     * @param <T>  类型参数
     * @return 可空值
     */
    public <T> Optional<T> getOptional(String name) {
        return Optional.ofNullable(get(name));
    }

    /**
     * 根据索引获取一个字符串
     *
     * @param index 索引
     * @return 字符串或null
     */
    public String getString(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * 根据索引获取一个可为空的字符串
     *
     * @param index 索引
     * @return 可空字符串
     */
    public Optional<String> getOptionalString(int index) {
        return Optional.ofNullable(getString(index));
    }

    /**
     * 根据名字获取一个字符串
     *
     * @param name 名字
     * @return 字符串或null
     */
    public String getString(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getString(index);
    }

    /**
     * 根据名字获取一个可为空的字符串
     *
     * @param name 名字
     * @return 可空字符串
     */
    public Optional<String> getOptionalString(String name) {
        return Optional.ofNullable(getString(name));
    }

    /**
     * 根据索引获取一个整型
     *
     * @param index 索引
     * @return 整型或null
     */
    public Integer getInt(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return Integer.parseInt(value.toString());
    }

    /**
     * 根据索引获取一个可为空的整型
     *
     * @param index 索引
     * @return 可空整型
     */
    public Optional<Integer> getOptionalInt(int index) {
        return Optional.ofNullable(getInt(index));
    }

    /**
     * 根据名字获取一个整型
     *
     * @param name 名字
     * @return 整型或null
     */
    public Integer getInt(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getInt(index);
    }

    /**
     * 根据名字获取一个可为空的整型
     *
     * @param name 名字
     * @return 可空整型
     */
    public Optional<Integer> getOptionalInt(String name) {
        return Optional.ofNullable(getInt(name));
    }

    /**
     * 获取一个双精度类型数组
     *
     * @param index 索引
     * @return 双精度数字或null
     */
    public Double getDouble(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * 获取一个可空双精度类型数组
     *
     * @param index 索引
     * @return 可空双精度数字
     */
    public Optional<Double> getOptionalDouble(int index) {
        return Optional.ofNullable(getDouble(index));
    }

    /**
     * 获取一个双精度类型数组
     *
     * @param name 名字
     * @return 双精度数字
     */
    public Double getDouble(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getDouble(index);
    }

    /**
     * 获取一个双精度类型数组
     *
     * @param name 名字
     * @return 可空双精度数字
     */
    public Optional<Double> getOptionalDouble(String name) {
        return Optional.ofNullable(getDouble(name));
    }

    /**
     * 获取一个长整型值
     *
     * @param index 索引
     * @return 双精度数字
     */
    public Long getLong(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 获取一个可空长整型值
     *
     * @param index 索引
     * @return 可空长整型值
     */
    public Optional<Long> getOptionalLong(int index) {
        return Optional.ofNullable(getLong(index));
    }

    /**
     * 获取一个长整型值
     *
     * @param name 名字
     * @return 双精度数字
     */
    public Long getLong(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getLong(index);
    }

    /**
     * 获取一个可空长整型值
     *
     * @param name 名字
     * @return 可空长整型值
     */
    public Optional<Long> getOptionalLong(String name) {
        return Optional.ofNullable(getLong(name));
    }

    /**
     * 行数据列数
     *
     * @return 列数
     */
    public int size() {
        return values.length;
    }

    /**
     * 获取指定字段名的索引
     *
     * @param name 字段名
     * @return 字段名位置索引
     */
    public int indexOf(String name) {
        int index = -1;
        if (name == null) {
            return index;
        }
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 判断键名是否存在
     *
     * @param name 键名
     * @return 键名是否存在
     */
    public boolean containsName(String name) {
        return indexOf(name) != -1;
    }

    /**
     * 合并一个数据行
     *
     * @param other 另一个数据行
     * @return 新的数据行
     */
    public DataRow concat(DataRow other) {
        String[] newNames = new String[size() + other.size()];
        Object[] newValues = new Object[newNames.length];
        System.arraycopy(names, 0, newNames, 0, names.length);
        System.arraycopy(other.names, 0, newNames, names.length, other.names.length);

        System.arraycopy(values, 0, newValues, 0, values.length);
        System.arraycopy(other.values, 0, newValues, values.length, other.values.length);

        return DataRow.of(newNames, newValues);
    }

    /**
     * 添加一个键值对
     *
     * @param name  字段名
     * @param value 值
     * @return 一个新的数据行
     */
    public DataRow add(String name, Object value) {
        return concat(DataRow.fromPair(name, value));
    }

    /**
     * 根据字段名移除一个值
     *
     * @param name 字段名
     * @return 一个新的数据行
     */
    public DataRow remove(String name) {
        int idx = indexOf(name);
        if (idx != -1) {
            String[] newNames = new String[size() - 1];
            Object[] newValues = new Object[newNames.length];

            if (idx == 0) {
                System.arraycopy(names, 1, newNames, 0, newNames.length);
            } else if (idx == newNames.length) {
                System.arraycopy(names, 0, newNames, 0, newNames.length);
            } else {
                System.arraycopy(names, 0, newNames, 0, idx);
                System.arraycopy(names, idx + 1, newNames, idx, newNames.length - idx);
            }

            for (int i = 0; i < newNames.length; i++) {
                String newName = newNames[i];
                newValues[i] = get(newName);
            }
            return DataRow.of(newNames, newValues);
        }
        throw new NoSuchElementException("name:\"" + name + "\" does not exist!");
    }

    /**
     * 转换为Map
     *
     * @param mapper 值转换器
     * @param <T>    值类型参数
     * @return 一个Map
     */
    public <T> Map<String, T> toMap(Function<Object, T> mapper) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], mapper.apply(get(i)));
        }
        return map;
    }

    /**
     * 转为Map
     *
     * @return map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            map.put(names[i], get(i));
        }
        return map;
    }

    /**
     * 转为一个标准的javaBean实体
     *
     * @param clazz                 实体类
     * @param <T>                   类型参数
     * @param constructorParameters 如果实体类只有一个带参数的构造函数，则指定参数<br>
     *                              e.g.
     *                              <blockquote>
     *                              <pre>DataRow row = DataRow.fromPair("x", 2, "y", 5, ...);</pre>
     *                              <pre>row.toEntity(A.class, row.get("x"), row.get("y"));</pre>
     *                              </blockquote>
     * @return 实体
     */
    public <T> T toEntity(Class<T> clazz, Object... constructorParameters) {
        try {
            T entity = ReflectUtil.getInstance(clazz, constructorParameters);
            for (Method method : ReflectUtil.getWRMethods(clazz).getItem2()) {
                if (method.getName().startsWith("set")) {
                    String field = method.getName().substring(3);
                    field = field.substring(0, 1).toLowerCase().concat(field.substring(1));
                    Object value = get(field);
                    // dataRow field type
                    String drValueType = getType(field);
                    if (value != null && drValueType != null && method.getParameterCount() == 1) {
                        // entity field type
                        Class<?> enFieldType = method.getParameterTypes()[0];
                        if (enFieldType == String.class) {
                            method.invoke(entity, value.toString());
                        } else if (enFieldType == Character.class || enFieldType == char.class) {
                            if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, value.toString().charAt(0));
                            } else {
                                method.invoke(entity, value);
                            }
                        } else if (enFieldType == Integer.class || enFieldType == int.class) {
                            if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, Integer.parseInt(value.toString()));
                            } else if (drValueType.equals("java.math.BigDecimal")) {
                                method.invoke(entity, ((BigDecimal) value).intValue());
                            } else {
                                method.invoke(entity, value);
                            }
                        } else if (enFieldType == Long.class || enFieldType == long.class) {
                            if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, Long.parseLong(value.toString()));
                            } else if (drValueType.equals("java.math.BigDecimal")) {
                                method.invoke(entity, ((BigDecimal) value).longValue());
                            } else {
                                method.invoke(entity, value);
                            }
                        } else if (enFieldType == Double.class || enFieldType == double.class) {
                            if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, Double.parseDouble(value.toString()));
                            } else if (drValueType.equals("java.math.BigDecimal")) {
                                method.invoke(entity, ((BigDecimal) value).doubleValue());
                            } else {
                                method.invoke(entity, value);
                            }
                        } else if (enFieldType == Float.class || enFieldType == float.class) {
                            if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, Float.parseFloat(value.toString()));
                            } else if (drValueType.equals("java.math.BigDecimal")) {
                                method.invoke(entity, ((BigDecimal) value).floatValue());
                            } else {
                                method.invoke(entity, value);
                            }
                            // if entity field type is java Date type
                        } else if (enFieldType == Date.class) {
                            // just set child class, 'java.sql.' date time type
                            if (Date.class.isAssignableFrom(value.getClass())) {
                                method.invoke(entity, value);
                            } else if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, DateTimes.toDate(value.toString()));
                            } else {
                                method.invoke(entity, value);
                            }
                            //if entity field type is java8 new date time api，convert sql date time type
                        } else if (Temporal.class.isAssignableFrom(enFieldType)) {
                            switch (drValueType) {
                                case "java.sql.Date":
                                    if (enFieldType == LocalDate.class) {
                                        method.invoke(entity, ((java.sql.Date) value).toLocalDate());
                                    }
                                    break;
                                case "java.sql.Timestamp":
                                    if (enFieldType == LocalDateTime.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime());
                                    } else if (enFieldType == Instant.class) {
                                        method.invoke(entity, ((Timestamp) value).toInstant());
                                    } else if (enFieldType == LocalDate.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime().toLocalDate());
                                    } else if (enFieldType == LocalTime.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime().toLocalTime());
                                    }
                                    break;
                                case "java.sql.Time":
                                    if (enFieldType == LocalTime.class) {
                                        method.invoke(entity, ((Time) value).toLocalTime());
                                    }
                                    break;
                                case "java.util.Date":
                                    ZonedDateTime zoneDt = ((Date) value).toInstant().atZone(ZoneId.systemDefault());
                                    if (enFieldType == LocalDateTime.class) {
                                        method.invoke(entity, zoneDt.toLocalDateTime());
                                    } else if (enFieldType == Instant.class) {
                                        method.invoke(entity, zoneDt.toInstant());
                                    } else if (enFieldType == LocalDate.class) {
                                        method.invoke(entity, zoneDt.toLocalDate());
                                    } else if (enFieldType == LocalTime.class) {
                                        method.invoke(entity, zoneDt.toLocalTime());
                                    }
                                    break;
                                case "java.lang.String":
                                    if (enFieldType == LocalDateTime.class) {
                                        method.invoke(entity, DateTimes.toLocalDateTime(value.toString()));
                                    } else if (enFieldType == Instant.class) {
                                        method.invoke(entity, DateTimes.toInstant(value.toString()));
                                    } else if (enFieldType == LocalDate.class) {
                                        method.invoke(entity, DateTimes.toLocalDate(value.toString()));
                                    } else if (enFieldType == LocalTime.class) {
                                        method.invoke(entity, DateTimes.toLocalTime(value.toString()));
                                    }
                                    break;
                                default:
                                    method.invoke(entity, value);
                                    break;
                            }
                            // if entity filed type is Map, Collection, or not starts with java.
                            // reason：about not starts with java, allow Map, Collection, user's custom entity, except others.
                        } else if (Map.class.isAssignableFrom(enFieldType) || Collection.class.isAssignableFrom(enFieldType) || !enFieldType.getTypeName().startsWith("java.")) {
                            // if dataRow from PostgreSQL
                            if (drValueType.equals("org.postgresql.util.PGobject")) {
                                Class<?> pgObjClass = value.getClass();
                                String pgType = (String) pgObjClass.getDeclaredMethod("getType").invoke(value);
                                String pgValue = (String) pgObjClass.getDeclaredMethod("getValue").invoke(value);
                                if (pgType.equals("json") || pgType.equals("jsonb")) {
                                    method.invoke(entity, json2Obj(pgValue, enFieldType));
                                }
                                // I think this is json string and you want convert to object.
                            } else if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, json2Obj(value.toString(), enFieldType));
                                // PostgreSQL array and exclude blob
                            } else if (drValueType.startsWith("[L") && drValueType.endsWith(";")) {
                                if (List.class.isAssignableFrom(enFieldType)) {
                                    method.invoke(entity, Arrays.asList((Object[]) value));
                                } else if (Set.class.isAssignableFrom(enFieldType)) {
                                    method.invoke(entity, new HashSet<>(Arrays.asList((Object[]) value)));
                                } else {
                                    method.invoke(entity, value);
                                }
                            } else {
                                method.invoke(entity, value);
                            }
                        } else {
                            method.invoke(entity, value);
                        }
                    }
                }
            }
            return entity;
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IntrospectionException | IllegalAccessException e) {
            throw new RuntimeException("convert to " + clazz.getTypeName() + " error: ", e);
        }
    }

    /**
     * 转为一个json字符串
     *
     * @return json字符串
     */
    public String toJson() {
        return obj2Json(toMap());
    }

    /**
     * 组合多个DataRow以创建一个DataRow（数据行转为列）
     *
     * @param rows 数据行集合 （为保证正确性，默认以第一行字段为列名，每行字段都必须相同）
     * @return 一行以列存储形式的数据行
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    public static DataRow zip(Collection<DataRow> rows) {
        if (rows.isEmpty()) {
            return empty();
        }
        if (rows.size() == 1) {
            return rows.iterator().next();
        }
        boolean first = true;
        DataRow res = null;
        String[] names = null;
        for (DataRow row : rows) {
            if (first) {
                names = row.names;
                Object[] pairs = new Object[names.length << 1];
                for (int i = 0; i < names.length; i++) {
                    int vi = i << 1;
                    pairs[vi] = names[i];
                    pairs[vi + 1] = new ArrayList<>();
                }
                res = DataRow.fromPair(pairs);
                first = false;
            }

            for (String name : names) {
                ((ArrayList<Object>) res.get(name)).add(row.get(name));
            }
        }
        return res;
    }

    /**
     * 从一个标准的javaBean实体转为DataRow类型
     *
     * @param entity 实体
     * @return DataRow
     */
    public static DataRow fromEntity(Object entity) {
        try {
            List<String> names = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            for (Method method : ReflectUtil.getWRMethods(entity.getClass()).getItem1()) {
                Class<?> returnType = method.getReturnType();
                if (returnType != Class.class) {
                    String field = method.getName();
                    if (field.startsWith("get")) {
                        field = field.substring(3);
                    } else if (field.startsWith("is")) {
                        field = field.substring(2);
                    }
                    Object value = method.invoke(entity);
                    if (value != null) {
                        field = field.substring(0, 1).toLowerCase().concat(field.substring(1));
                        names.add(field);
                        values.add(value);
                    }
                }
            }
            return of(names.toArray(new String[0]), values.toArray());
        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
            throw new RuntimeException("convert to DataRow error: ", e);
        }
    }

    /**
     * 从map转换到DataRow
     *
     * @param map map
     * @return DataRow
     */
    public static DataRow fromMap(Map<?, ?> map) {
        String[] names = new String[map.keySet().size()];
        Object[] values = new Object[names.length];
        int i = 0;
        for (Object key : map.keySet()) {
            names[i] = key.toString();
            values[i] = map.get(key);
            i++;
        }
        return of(names, values);
    }

    /**
     * 从一组键值对创建一个DataRow
     *
     * @param pairs 键值对 k v，k v...
     * @return DataRow
     */
    public static DataRow fromPair(Object... pairs) {
        if (pairs.length == 0 || (pairs.length & 1) != 0) {
            throw new IllegalArgumentException("key value are not a pair.");
        }
        String[] names = new String[pairs.length >> 1];
        Object[] values = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = pairs[i << 1].toString();
            values[i] = pairs[(i << 1) + 1];
        }
        return of(names, values);
    }

    /**
     * 从一个json对象字符串创建一个DataRow
     *
     * @param json json对象字符串 e.g. {@code {"a":1,"b":2}}
     * @return DataRow
     */
    public static DataRow fromJson(String json) {
        return fromMap((Map<?, ?>) json2Obj(json, Map.class));
    }

    /**
     * 遍历名称和值
     *
     * @param consumer 回调函数
     */
    public void foreach(BiConsumer<String, Object> consumer) {
        for (int i = 0; i < size(); i++) {
            consumer.accept(names[i], values[i]);
        }
    }

    /**
     * 归并操作
     *
     * @param init   初始值
     * @param mapper 映射(初始值，名字，值)
     * @param <T>    结果类型参数
     * @return 归并后的结果
     */
    public <T> T reduce(T init, TiFunction<T, String, Object, T> mapper) {
        T acc = init;
        for (int i = 0; i < size(); i++) {
            acc = mapper.apply(acc, names[i], values[i]);
        }
        return acc;
    }

    @Override
    public String toString() {
        String[] types = new String[values.length];
        for (int i = 0; i < types.length; i++) {
            types[i] = getType(i);
        }
        return "DataRow{\n" +
                "names=" + Arrays.toString(names) +
                "\ntypes=" + Arrays.toString(types) +
                "\nvalues=" + Arrays.toString(values) +
                "\n}";
    }
}
