package tests;

import com.github.chengyuxing.common.DateTimes;
import com.github.chengyuxing.common.TiFunction;
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
 * 一个同时支持根据键和索引高效取值的（{@code key|index-value}）容器<br>
 * <blockquote>
 * {@link #getString(String)}<br>
 * {@link #getString(int)}
 * </blockquote>
 * 值存储基于数组，自动扩容机制和{@link ArrayList}类似，实现{@link Map}接口，支持{@code json}序列化，
 * 内部使用{@link LinkedHashMap}来维护键的顺序和值的索引，这是一个非线程安全的实现。
 */
public final class DataRowX implements Map<String, Object> {
    private Entry<String, Object>[] elementData;
    private int size;

    /**
     * 一个空的DataRow（初始化大小为16）
     */
    @SuppressWarnings("unchecked")
    public DataRowX() {
        elementData = (Entry<String, Object>[]) Array.newInstance(Entry.class, 16);
    }

    /**
     * 一个空的DataRow
     *
     * @param capacity 初始容量大小
     */
    @SuppressWarnings("unchecked")
    public DataRowX(int capacity) {
        elementData = (Entry<String, Object>[]) Array.newInstance(Entry.class, capacity);
    }

    /**
     * @return 一个空的DataRow（初始化大小为16）
     */
    public static DataRowX empty() {
        return new DataRowX();
    }

    /**
     * 新建一个DataRow
     *
     * @param names  一组字段名
     * @param values 一组值
     * @return 新实例，初始化小为字段名数组的长度
     */
    public static DataRowX of(String[] names, Object[] values) {
        if (names.length == values.length) {
            if (names.length == 0) {
                return new DataRowX(0);
            }
            DataRowX row = new DataRowX(names.length);
            for (int i = 0; i < names.length; i++) {
                row.put(names[i], values[i]);
            }
            return row;
        }
        throw new IllegalArgumentException("keys and values length not equal!");
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
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(elementData[i].getKey());
        }
        return list;
    }

    /**
     * 根据字段名获取类型
     *
     * @param name 字段
     * @return 如果值存在或不为null返回值类型名称，否则返回null
     */
    public Class<?> getType(String name) {
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
    public Class<?> getType(int index) {
        Object v = elementData[index].getValue();
        if (v == null) {
            return null;
        }
        return v.getClass();
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
        return (T) elementData[index].getValue();
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
            for (int i = 0; i < size; i++)
                if (elementData[i].getKey() == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (name.equals(elementData[i].getKey()))
                    return i;
        }
        return -1;
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
        return indexOf(key) != -1;
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < size; i++) {
            if (value.equals(elementData[i].getValue())) {
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
        return elementData[index].getValue();
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
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>(names());
    }

    @Override
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(elementData[i].getValue());
        }
        return list;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new HashSet<>(Arrays.asList(elementData));
    }

    @Override
    public Object put(String key, Object value) {
        return addOrUpdate(key, value, true);
    }

    @Override
    public Object remove(Object key) {
        int index = indexOf(key);
        if (index != -1) {
            Object old = elementData[index].getValue();
            int numMoved = size - index - 1;
            if (numMoved > 0) {
                System.arraycopy(elementData, index + 1, elementData, index, numMoved);
            }
            int dIncIdx = --size;
            elementData[dIncIdx] = null;
            return old;
        }
        return null;
    }

    /**
     * 移除值为null的所有元素
     *
     * @return 不存在null值的当前对象
     */
    public DataRowX removeIfAbsent() {
        for (int index = 0; index < size; index++) {
            if (elementData[index].getValue() == null) {
                int numMoved = size - index - 1;
                if (numMoved > 0) {
                    System.arraycopy(elementData, index + 1, elementData, index, numMoved);
                }
                int dIncIdx = --size;
                elementData[dIncIdx] = null;
            }
        }
        return this;
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        for (int i = 0; i < size; i++) {
            String k;
            Object v;
            try {
                k = elementData[i].getKey();
                v = elementData[i].getValue();
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
    @SuppressWarnings("unchecked")
    private Object addOrUpdate(String name, Object value, boolean update) {
//        if (update) {
//            int idx = indexOf(name);
//            if (idx != -1) {
//                elementData[idx].setValue(value);
//                return value;
//            }
//        }
        if (size < elementData.length) {
            int incIdx = size++;
            elementData[incIdx] = new Entry<String, Object>() {
                @Override
                public String getKey() {
                    return name;
                }

                @Override
                public Object getValue() {
                    return value;
                }

                @Override
                public Object setValue(Object newValue) {
                    Object oldValue = elementData[incIdx].getValue();
                    elementData[incIdx].setValue(newValue);
                    return oldValue;
                }
            };
        } else {
            int oldCapacity = elementData.length;
            int newCapacity = oldCapacity << 1;
            Entry<String, Object>[] newElementData = (Entry<String, Object>[]) Array.newInstance(Entry.class, newCapacity);
            System.arraycopy(elementData, 0, newElementData, 0, oldCapacity);
            int incIdx = size++;
            newElementData[incIdx] = new Entry<String, Object>() {
                @Override
                public String getKey() {
                    return name;
                }

                @Override
                public Object getValue() {
                    return value;
                }

                @Override
                public Object setValue(Object newValue) {
                    Object oldValue = elementData[incIdx].getValue();
                    elementData[incIdx].setValue(newValue);
                    return oldValue;
                }
            };
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
    public DataRowX set(String name, Object value) {
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
    public DataRowX pick(String name, String... more) {
        DataRowX row = new DataRowX(more.length + 1);
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
        for (int i = 0; i < size; i++) {
            acc = mapper.apply(acc, elementData[i].getKey(), elementData[i].getValue());
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
        for (int i = 0; i < size; i++) {
            map.put(elementData[i].getKey(), mapper.apply(elementData[i]));
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
        for (int i = 0; i < size; i++) {
            map.put(elementData[i].getKey(), elementData[i]);
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
     *                              <pre>DataRowX row = DataRowX.fromPair("x", 2, "y", 5, ...);</pre>
     *                              <pre>row.toEntity(A.class, row.get("x"), row.get("y"));</pre>
     *                              </blockquote>
     * @return 实体
     */
    public <T> T toEntity(Class<T> clazz, Object... constructorParameters) {
        try {
            T entity = ReflectUtil.getInstance(clazz, constructorParameters);
            for (Method method : ReflectUtil.getRWMethods(clazz).getItem2()) {
                if (method.getName().startsWith("set")) {
                    String field = method.getName().substring(3);
                    field = field.substring(0, 1).toLowerCase().concat(field.substring(1));
                    Object value = get(field);
                    // dataRow field type
                    Class<?> drValueClass = getType(field);
                    if (value != null && drValueClass != null && method.getParameterCount() == 1) {
                        String drValueType = drValueClass.getName();
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
    public static DataRowX zip(Collection<DataRowX> rows) {
        if (rows.isEmpty()) {
            return new DataRowX(0);
        }
        if (rows.size() == 1) {
            return rows.iterator().next();
        }
        boolean first = true;
        DataRowX res = null;
        List<String> names = null;
        for (DataRowX row : rows) {
            if (first) {
                res = new DataRowX(row.size);
                names = row.names();
                for (String name : names) {
                    res.addOrUpdate(name, new ArrayList<>(), false);
                }
                first = false;
            }
            for (String name : names) {
                ((ArrayList<Object>) res.getAs(name)).add(row.get(name));
            }
        }
        return res;
    }

    /**
     * 从一个标准的javaBean实体转为DataRow类型
     *
     * @param entity 实体
     * @return DataRowX
     */
    public static DataRowX fromEntity(Object entity) {
        try {
            DataRowX row = DataRowX.empty();
            for (Method method : ReflectUtil.getRWMethods(entity.getClass()).getItem1()) {
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
            throw new RuntimeException("convert to DataRowX error: ", e);
        }
    }

    /**
     * 从map转换到DataRow
     *
     * @param map map
     * @return DataRowX
     */
    public static DataRowX fromMap(Map<?, ?> map) {
        DataRowX row = new DataRowX(map.size());
        for (Object key : map.keySet()) {
            row.addOrUpdate(key.toString(), map.get(key), false);
        }
        return row;
    }

    /**
     * 从一组键值对创建一个DataRow
     *
     * @param pairs 键值对 k v，k v...
     * @return DataRowX
     */
    public static DataRowX fromPair(Object... pairs) {
        if (pairs.length == 0 || (pairs.length & 1) != 0) {
            throw new IllegalArgumentException("key value are not a pair.");
        }
        int capacity = pairs.length >> 1;
        DataRowX row = new DataRowX(capacity);
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
     * @return DataRowX
     */
    public static DataRowX fromJson(String json) {
        return (DataRowX) ReflectUtil.json2Obj(json, DataRowX.class);
    }

    @Override
    public String toString() {
        String[] types = new String[size];
        for (int i = 0; i < size; i++) {
            Class<?> type = getType(i);
            String typeName = type == null ? "unKnow" : type.getName();
            types[i] = typeName;
        }
        return "DataRowX{\n" +
                "names=" + names() +
                "\ntypes=" + Arrays.toString(types) +
                "\nvalues=" + values() +
                "\n}";
    }
}
