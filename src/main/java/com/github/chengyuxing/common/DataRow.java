package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.ReflectUtil;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.github.chengyuxing.common.utils.ReflectUtil.json2Obj;

/**
 * 不可变行数据类型
 */
public final class DataRow {
    private final String[] names;
    private final String[] types;
    private final Object[] values;

    DataRow(String[] names, String[] types, Object[] values) {
        this.names = names;
        this.types = types;
        this.values = values;
    }

    /**
     * 新建一个DataRow
     *
     * @param names  字段
     * @param types  类型
     * @param values 值
     * @return 新实例
     */
    public static DataRow of(String[] names, String[] types, Object[] values) {
        if (names.length == types.length && types.length == values.length) {
            return new DataRow(names, types, values);
        }
        throw new IllegalArgumentException("all of 3 args length not equal!");
    }

    /**
     * 新建一个DataRow
     *
     * @param names  字段
     * @param values 值
     * @return 新实例
     */
    public static DataRow of(String[] names, Object[] values) {
        String[] types = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                types[i] = "null";
            } else {
                types[i] = values[i].getClass().getName();
            }
        }
        return of(names, types, values);
    }

    /**
     * @return 一个空的DataRow
     */
    public static DataRow empty() {
        return of(new String[0], new String[0], new Object[0]);
    }

    /**
     * 判断是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return names.length == 0;
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
     * 获取类型
     *
     * @return 类型
     */
    public List<String> getTypes() {
        return Arrays.asList(types);
    }

    /**
     * 根据字段名获取类型
     *
     * @param name 字段
     * @return 类型
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
     * @return 值类型
     */
    public String getType(int index) {
        return types[index];
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
        String[] newTypes = new String[newNames.length];
        System.arraycopy(names, 0, newNames, 0, names.length);
        System.arraycopy(other.names, 0, newNames, names.length, other.names.length);

        System.arraycopy(values, 0, newValues, 0, values.length);
        System.arraycopy(other.values, 0, newValues, values.length, other.values.length);

        System.arraycopy(types, 0, newTypes, 0, types.length);
        System.arraycopy(other.types, 0, newTypes, types.length, other.types.length);

        return DataRow.of(newNames, newTypes, newValues);
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
            String[] newTypes = new String[newNames.length];
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
                newTypes[i] = getType(newName);
                newValues[i] = get(newName);
            }
            return DataRow.of(newNames, newTypes, newValues);
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
     * @param clazz 实体类
     * @param <T>   类型参数
     * @return 实体
     */
    public <T> T toEntity(Class<T> clazz) {
        try {
            T entity = clazz.newInstance();
            Iterator<Method> methods = ReflectUtil.getWriteMethods(clazz).iterator();
            while (methods.hasNext()) {
                Method method = methods.next();
                if (method.getName().startsWith("set")) {
                    String field = method.getName().substring(3);
                    field = field.substring(0, 1).toLowerCase().concat(field.substring(1));
                    Object value = get(field);
                    String valueType = getType(field);
                    if (value != null && valueType != null) {
                        Class<?> argType = method.getParameterTypes()[0];
                        if (argType == Date.class) {
                            switch (valueType) {
                                case "java.sql.Time":
                                    method.invoke(entity, new Date(((Time) value).toLocalTime().atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                                    break;
                                case "java.sql.Timestamp":
                                    method.invoke(entity, new Date(((Timestamp) value).toInstant().toEpochMilli()));
                                    break;
                                case "java.sql.Date":
                                    method.invoke(entity, new Date(((java.sql.Date) value).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                                    break;
                                case "java.lang.String":
                                    method.invoke(entity, DateTimes.toDate(value.toString()));
                                    break;
                            }
                        } else if (Temporal.class.isAssignableFrom(argType)) {
                            switch (valueType) {
                                case "java.sql.Date":
                                    if (argType == LocalDate.class) {
                                        method.invoke(entity, ((java.sql.Date) value).toLocalDate());
                                    }
                                    break;
                                case "java.sql.Timestamp":
                                    if (argType == LocalDateTime.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime());
                                    } else if (argType == Instant.class) {
                                        method.invoke(entity, ((Timestamp) value).toInstant());
                                    } else if (argType == LocalDate.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime().toLocalDate());
                                    } else if (argType == LocalTime.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime().toLocalTime());
                                    }
                                    break;
                                case "java.sql.Time":
                                    if (argType == LocalTime.class) {
                                        method.invoke(entity, ((Time) value).toLocalTime());
                                    }
                                    break;
                                case "java.lang.String":
                                    if (argType == LocalDateTime.class) {
                                        method.invoke(entity, DateTimes.toLocalDateTime(value.toString()));
                                    } else if (argType == Instant.class) {
                                        method.invoke(entity, DateTimes.toInstant(value.toString()));
                                    } else if (argType == LocalDate.class) {
                                        method.invoke(entity, DateTimes.toLocalDate(value.toString()));
                                    } else if (argType == LocalTime.class) {
                                        method.invoke(entity, DateTimes.toLocalTime(value.toString()));
                                    }
                                    break;
                            }
                        } else if (Map.class.isAssignableFrom(argType) || Collection.class.isAssignableFrom(argType) || !argType.getTypeName().startsWith("java.")) {
                            if (valueType.equals("org.postgresql.util.PGobject")) {
                                Class<?> pgObjClass = value.getClass();
                                String pgType = (String) pgObjClass.getDeclaredMethod("getType").invoke(value);
                                String pgValue = (String) pgObjClass.getDeclaredMethod("getValue").invoke(value);
                                if (pgType.equals("json") || pgType.equals("jsonb")) {
                                    method.invoke(entity, json2Obj(pgValue, argType));
                                }
                                // I think this is json string and you want convert to object.
                            } else if (valueType.equals("java.lang.String")) {
                                method.invoke(entity, json2Obj(value.toString(), argType));
                                // PostgreSQL array and exclude blob
                            } else if (valueType.startsWith("[L") || (valueType.endsWith("[]") && !valueType.equals("byte[]"))) {
                                if (List.class.isAssignableFrom(argType)) {
                                    method.invoke(entity, Arrays.asList((Object[]) value));
                                } else if (Set.class.isAssignableFrom(argType)) {
                                    method.invoke(entity, new HashSet<>(Arrays.asList((Object[]) value)));
                                }
                            } else {
                                method.invoke(entity, value);
                            }
                        } else if (argType == String.class) {
                            method.invoke(entity, value.toString());
                        } else {
                            method.invoke(entity, value);
                        }
                    }
                }
            }
            return entity;
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IntrospectionException | IllegalAccessException e) {
            throw new RuntimeException("convert to " + clazz.getTypeName() + "error: ", e);
        }
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
            List<String> types = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            Iterator<Method> methods = ReflectUtil.getReadMethods(entity.getClass()).iterator();
            while (methods.hasNext()) {
                Method method = methods.next();
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
                        String type = returnType.getTypeName();
                        field = field.substring(0, 1).toLowerCase().concat(field.substring(1));
                        names.add(field);
                        types.add(type);
                        values.add(value);
                    }
                }
            }
            return of(names.toArray(new String[0]), types.toArray(new String[0]), values.toArray());
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
        String[] types = new String[names.length];
        Object[] values = new Object[names.length];
        int i = 0;
        for (Object key : map.keySet()) {
            names[i] = key.toString();
            values[i] = map.get(key);
            if (values[i] == null) {
                types[i] = "null";
            } else {
                types[i] = values[i].getClass().getName();
            }
            i++;
        }
        return of(names, types, values);
    }

    /**
     * 从一组键值对创建一个DataRow
     *
     * @param pairs 键值对 k v，k v...
     * @return DataRow
     */
    public static DataRow fromPair(Object... pairs) {
        if (pairs.length == 0 || pairs.length % 2 != 0) {
            throw new IllegalArgumentException("key value are not a pair.");
        }
        String[] names = new String[pairs.length >> 1];
        String[] types = new String[names.length];
        Object[] values = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = pairs[i << 1].toString();
            values[i] = pairs[(i << 1) + 1];
            if (values[i] == null) {
                types[i] = "null";
            } else {
                types[i] = values[i].getClass().getName();
            }
        }
        return of(names, types, values);
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
     * 归并操作符
     *
     * @param mapper 映射(初始值类型，名字，值)
     * @param init   初始值
     * @param <T>    结果类型参数
     * @return 归并后的结果
     */
    public <T> T reduce(TiFunction<T, String, Object, T> mapper, T init) {
        T acc = init;
        for (int i = 0; i < size(); i++) {
            acc = mapper.apply(acc, names[i], values[i]);
        }
        return acc;
    }

    @Override
    public String toString() {
        return "DataRow{" +
                "names=" + Arrays.toString(names) +
                ", types=" + Arrays.toString(types) +
                ", values=" + Arrays.toString(values) +
                '}';
    }
}
