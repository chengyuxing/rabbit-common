package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.ReflectUtil;

import java.beans.IntrospectionException;
import java.lang.reflect.Array;
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

/**
 * <h1>行数据对象</h1>
 * 值存储基于自动扩容数组，逻辑和ArrayList类似，实现Map接口，同时支持根据键和索引高效取值，支持json序列化<br>
 * 注意：这是一个非线程安全的实现
 */
public final class DataRow implements Map<String, Object> {
    private final Map<String, Integer> indices;
    private Object[] elementData;
    private int size;

    /**
     * 一个空的DataRow（初始化大小为16）
     */
    public DataRow() {
        elementData = (Object[]) Array.newInstance(Object.class, 16);
        indices = new LinkedHashMap<>(16);
    }

    /**
     * 一个空的DataRow
     *
     * @param capacity 初始容量大小
     */
    public DataRow(int capacity) {
        elementData = (Object[]) Array.newInstance(Object.class, capacity);
        indices = new LinkedHashMap<>(capacity);
    }

    /**
     * @return 一个空的DataRow（初始化大小为16）
     */
    public static DataRow empty() {
        return new DataRow();
    }

    /**
     * 新建一个DataRow
     *
     * @param names  一组字段名
     * @param values 一组值
     * @return 新实例，初始化小为字段名数组的长度
     */
    public static DataRow of(String[] names, Object[] values) {
        if (names.length == values.length) {
            if (names.length == 0) {
                return empty();
            }
            DataRow row = new DataRow(names.length);
            for (int i = 0; i < names.length; i++) {
                row.put(names[i], values[i]);
            }
            return row;
        }
        throw new IllegalArgumentException("all of 3 args length not equal!");
    }

    /**
     * 获取值集合
     *
     * @return 值集合
     * @see #values()
     */
    @Deprecated
    public List<Object> getValues() {
        return (List<Object>) values();
    }

    /**
     * 获取字段名集合
     *
     * @return 字段名集合
     */
    public List<String> names() {
        return new ArrayList<>(indices.keySet());
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
        Object v = getAs(index);
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
    public <T> T getAs(int index) {
        return (T) elementData[index];
    }

    /**
     * 获取值
     *
     * @param name 名称
     * @param <T>  结果类型参数
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(String name) {
        return (T) get(name);
    }

    /**
     * 获取可空值
     *
     * @param index 索引
     * @param <T>   类型参数
     * @return 可空值
     */
    public <T> Optional<T> getOptional(int index) {
        return Optional.ofNullable(getAs(index));
    }

    /**
     * 获取可空值
     *
     * @param name 名字
     * @param <T>  类型参数
     * @return 可空值
     */
    public <T> Optional<T> getOptional(String name) {
        return Optional.ofNullable(getAs(name));
    }

    /**
     * 根据索引获取一个字符串
     *
     * @param index 索引
     * @return 字符串或null
     */
    public String getString(int index) {
        Object value = getAs(index);
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
        Object value = getAs(index);
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
        Object value = getAs(index);
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
        Object value = getAs(index);
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
     * 获取指定字段名的索引
     *
     * @param name 字段名
     * @return 字段名位置索引
     */
    public int indexOf(Object name) {
        if (name == null) {
            return -1;
        }
        return indices.getOrDefault(name.toString(), -1);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return indices.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < size; i++) {
            if (value.equals(elementData[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        int index = indexOf(key);
        if (index == -1) {
            return null;
        }
        return elementData[index];
    }

    /**
     * 清空所有元素
     */
    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }
        size = 0;
        indices.clear();
    }

    @Override
    public Set<String> keySet() {
        return indices.keySet();
    }

    @Override
    public Collection<Object> values() {
        return new ArrayList<>(Arrays.asList(elementData).subList(0, size));
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> set = new HashSet<>();
        int i = 0;
        for (String key : indices.keySet()) {
            set.add(new DataRowEntry(key, elementData[i++]));
        }
        return set;
    }

    @Override
    public Object put(String name, Object value) {
        return addOrUpdate(name, value, true);
    }

    @Override
    public Object remove(Object key) {
        if (key == null) {
            return null;
        }
        String name = key.toString();
        int index = indexOf(name);
        if (index != -1) {
            Object old = elementData[index];
            int numMoved = size - index - 1;
            if (numMoved > 0) {
                System.arraycopy(elementData, index + 1, elementData, index, numMoved);
            }
            int dIncIdx = --size;
            elementData[dIncIdx] = null;
            indices.remove(name);
            return old;
        }
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        int i = 0;
        for (String key : indices.keySet()) {
            String k;
            Object v;
            try {
                k = key;
                v = elementData[i++];
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    /**
     * 如果不存在就添加否则更新
     *
     * @param name   键名
     * @param value  值
     * @param update 是否进行更新
     * @return 更新后的值
     */
    private Object addOrUpdate(String name, Object value, boolean update) {
        if (update) {
            int idx = indexOf(name);
            if (idx != -1) {
                elementData[idx] = value;
                return value;
            }
        }
        if (size < elementData.length) {
            int incIdx = size++;
            elementData[incIdx] = value;
            indices.put(name, incIdx);
        } else {
            int oldCapacity = elementData.length;
            int newCapacity = oldCapacity << 1;
            Object[] newElementData = (Object[]) Array.newInstance(Object.class, newCapacity);
            System.arraycopy(elementData, 0, newElementData, 0, oldCapacity);
            int incIdx = size++;
            newElementData[incIdx] = value;
            indices.put(name, incIdx);
            elementData = newElementData;
        }
        return value;
    }

    /**
     * 链式添加一个键值对
     *
     * @param name  键名
     * @param value 值
     * @return 对象自身
     */
    public DataRow set(String name, Object value) {
        put(name, value);
        return this;
    }

    /**
     * 挑出一些字段名生成一个新的DataRow
     *
     * @param name 字段名
     * @param more 更多字段名
     * @return 新的DataRow
     */
    public DataRow pick(String name, String... more) {
        DataRow row = new DataRow(more.length + 1);
        row.put(name, getAs(name));
        if (more.length > 0) {
            for (String n : more) {
                row.put(n, get(n));
            }
        }
        return row;
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
        int i = 0;
        for (String key : indices.keySet()) {
            acc = mapper.apply(acc, key, elementData[i++]);
        }
        return acc;
    }

    /**
     * 转为一个新的LinkedHashMap
     *
     * @param mapper 值转换器
     * @param <T>    值类型参数
     * @return map
     */
    public <T> Map<String, T> toMap(Function<Object, T> mapper) {
        Map<String, T> map = new LinkedHashMap<>();
        int i = 0;
        for (String key : indices.keySet()) {
            map.put(key, mapper.apply(elementData[i++]));
        }
        return map;
    }

    /**
     * 转为一个新的LinkedHashMap
     *
     * @return map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        int i = 0;
        for (String key : indices.keySet()) {
            map.put(key, elementData[i++]);
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
                                    method.invoke(entity, ReflectUtil.json2Obj(pgValue, enFieldType));
                                }
                                // I think this is json string and you want convert to object.
                            } else if (drValueType.equals("java.lang.String")) {
                                method.invoke(entity, ReflectUtil.json2Obj(value.toString(), enFieldType));
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
        return ReflectUtil.obj2Json(this);
    }

    /**
     * 组合多个DataRow以创建一个DataRow（数据行转为列）
     *
     * @param rows 数据行集合 （为保证正确性，默认以第一行字段为列名，每行字段都必须相同）
     * @return 一行以列存储形式的数据行
     */
    @SuppressWarnings({"unchecked"})
    public static DataRow zip(Collection<DataRow> rows) {
        if (rows.isEmpty()) {
            return new DataRow(0);
        }
        if (rows.size() == 1) {
            return rows.iterator().next();
        }
        boolean first = true;
        DataRow res = null;
        List<String> names = null;
        for (DataRow row : rows) {
            if (first) {
                res = new DataRow(row.size);
                names = row.names();
                for (String name : names) {
                    res.addOrUpdate(name, new ArrayList<>(), false);
                }
                first = false;
            }
            for (String name : names) {
                ((ArrayList<Object>) res.getAs(name)).add(row.getAs(name));
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
            DataRow row = DataRow.empty();
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
                        row.addOrUpdate(field, value, false);
                    }
                }
            }
            return row;
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
        DataRow row = new DataRow(map.size());
        for (Object key : map.keySet()) {
            row.addOrUpdate(key.toString(), map.get(key), false);
        }
        return row;
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
        int capacity = pairs.length >> 1;
        DataRow row = new DataRow(capacity);
        for (int i = 0; i < capacity; i++) {
            int idx = i << 1;
            row.put(pairs[idx].toString(), pairs[idx + 1]);
        }
        return row;
    }

    /**
     * 从一个json对象字符串创建一个DataRow
     *
     * @param json json对象字符串 e.g. {@code {"a":1,"b":2}}
     * @return DataRow
     */
    public static DataRow fromJson(String json) {
        return (DataRow) ReflectUtil.json2Obj(json, DataRow.class);
    }

    /**
     * 数据行对象Entry简单实现
     */
    class DataRowEntry implements Entry<String, Object> {

        private final String k;
        private final Object v;

        DataRowEntry(String k, Object v) {
            this.k = k;
            this.v = v;
        }

        @Override
        public String getKey() {
            return k;
        }

        @Override
        public Object getValue() {
            return v;
        }

        @Override
        public Object setValue(Object value) {
            return put(k, value);
        }

        @Override
        public String toString() {
            return "{" + k + "=" + v + "}";
        }
    }

    @Override
    public String toString() {
        String[] types = new String[size];
        for (int i = 0; i < size; i++) {
            types[i] = getType(i);
        }
        return "DataRow{\n" +
                "names=" + names() +
                "\ntypes=" + Arrays.toString(types) +
                "\nvalues=" + values() +
                "\n}";
    }
}

