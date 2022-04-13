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
import java.util.function.Function;

import static com.github.chengyuxing.common.utils.ObjectUtil.getValue;
import static com.github.chengyuxing.common.utils.ReflectUtil.json2Obj;
import static com.github.chengyuxing.common.utils.ReflectUtil.obj2Json;

/**
 * 不可变行数据类型
 */
public final class DataRow extends LinkedHashMap<String, Object> {
    /**
     * 构造函数
     */
    public DataRow() {

    }

    /**
     * 构造函数
     *
     * @param map 从一个map创建
     */
    public DataRow(Map<String, ?> map) {
        super(map);
    }

    /**
     * 构造函数
     *
     * @param names  一组键名
     * @param values 一组键值
     */
    public DataRow(String[] names, Object[] values) {
        if (names.length == values.length) {
            for (int i = 0; i < names.length; i++) {
                put(names[i], values[i]);
            }
        } else
            throw new IllegalArgumentException("names and values length not equal!");
    }

    /**
     * 新建一个DataRow
     *
     * @param names  字段
     * @param values 值
     * @return 新实例
     */
    public static DataRow of(String[] names, Object[] values) {
        return new DataRow(names, values);
    }

    /**
     * @return 一个空的DataRow
     */
    public static DataRow empty() {
        return new DataRow();
    }

    /**
     * 如果有元素则判断为非空
     *
     * @return 是否为非空
     */
    public boolean nonEmpty() {
        return !isEmpty();
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public List<Object> getValues() {
        return new ArrayList<>(values());
    }

    /**
     * 获取字段
     *
     * @return 字段
     */
    public List<String> getNames() {
        return new ArrayList<>(keySet());
    }

    /**
     * 根据字段名获取类型
     *
     * @param name 字段
     * @return 如果值存在或不为null返回值类型名称，否则返回null
     */
    public String getType(String name) {
        Object v = get(name);
        if (v == null) {
            return null;
        }
        return v.getClass().getName();
    }

    /**
     * 获取值类型
     *
     * @param index 索引
     * @return 如果值不为null返回值类型名称，否则返回null
     */
    public String getType(int index) {
        return get(index).getClass().getName();
    }

    /**
     * 根据索引获取键名
     *
     * @param index 索引
     * @return 键名
     */
    public String getName(int index) {
        Object[] keys = keySet().toArray();
        String name = keys[index].toString();
        for (int i = keys.length - 1; i >= 0; i--) {
            keys[i] = null;
        }
        keys = null;
        return name;
    }

    /**
     * 获取值，如果为null，则返回默认值
     *
     * @param key          如果是Integer类型，则根据索引获取
     * @param defaultValue 默认值
     * @return 值或默认值
     */
    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        if (key instanceof Integer) {
            Object v = get(((Integer) key).intValue());
            if (v == null) {
                return defaultValue;
            }
            return v;
        }
        return super.getOrDefault(key, defaultValue);
    }

    /**
     * 获取值
     *
     * @param key 如果是Integer类型，则根据索引获取
     * @return 值
     * @see #get(int)
     */
    @Override
    public Object get(Object key) {
        if (key instanceof Integer) {
            return get(((Integer) key).intValue());
        }
        return super.get(key);
    }

    /**
     * 获取值
     *
     * @param index 索引
     * @return 值
     */
    public Object get(int index) {
        return get(getName(index));
    }

    /**
     * 获取值
     *
     * @param name 键名
     * @param <T>  结果类型参数
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(Object name) {
        return (T) get(name);
    }

    /**
     * 获取值
     *
     * @param index 索引¬
     * @param <T>   结果类型参数
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(int index) {
        return (T) get(index);
    }

    /**
     * 获取可空值
     *
     * @param index 索引
     * @return 可空值
     */
    public Optional<Object> getOptional(int index) {
        return Optional.ofNullable(get(index));
    }

    /**
     * 使用json路径表达式来取多层嵌套对象的值，以 "/" 开头，
     * <blockquote>
     * 字符串为对象键名，数字为数组索引，例如：
     * <pre>
     *       DataRow: {a:{b:[{name:cyx},{name:jack}]}}
     *       json路径表达式："/a/b/0/name"
     *       结果：cyx
     *           </pre>
     * </blockquote>
     *
     * @param jsonPathExp json路径表达式（{@code /a/b/0/name}）
     * @return 值
     * @see #at(Object, Object...)
     */
    public Object at(String jsonPathExp) {
        String pathsS = jsonPathExp;
        if (jsonPathExp.startsWith("/")) {
            pathsS = pathsS.substring(1);
        }
        Object[] paths = pathsS.split("/");
        Object[] more = Arrays.copyOfRange(paths, 1, paths.length);
        return at(paths[0], more);
    }

    /**
     * 使用路径数组成员表示路径来取多层嵌套对象的值
     * <blockquote>
     * 字符串为对象键名，数字为索引，例如：
     * <pre>
     *       DataRow: {a:{b:[{name:cyx},{name:jack}]}}
     *       路径数组成员："a","b",0,"name"
     *       结果：cyx
     *           </pre>
     * </blockquote>
     *
     * @param key  对象第一层的键
     * @param more 对象内层更多的键
     * @return 值
     * @see #at(String)
     */
    public Object at(Object key, Object... more) {
        Object obj = get(key);
        if (more.length == 0) {
            return obj;
        }
        try {
            for (Object o : more) {
                obj = getValue(obj, o.toString());
            }
            return obj;
        } catch (InvocationTargetException e) {
            throw new RuntimeException("invoke error:", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("cannot find field of getMethod: ", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("target object access denied: ", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("cannot find field：", e);
        }
    }

    /**
     * 获取可空值
     *
     * @param name 名字
     * @return 可空值
     */
    public Optional<Object> getOptional(String name) {
        return Optional.ofNullable(get(name));
    }

    /**
     * 根据名字获取一个字符串
     *
     * @param name 名字
     * @return 字符串或null
     */
    public String getString(String name) {
        Object v = get(name);
        if (v == null) {
            return null;
        }
        return v.toString();
    }

    /**
     * 根据索引获取一个字符串
     *
     * @param index 索引
     * @return 字符串或null
     */
    public String getString(int index) {
        return getString(getName(index));
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
     * 根据名字获取一个可为空的字符串
     *
     * @param name 名字
     * @return 可空字符串
     */
    public Optional<String> getOptionalString(String name) {
        return Optional.ofNullable(getString(name));
    }

    /**
     * 根据名字获取一个整型
     *
     * @param name 名字
     * @return 整型或null
     */
    public Integer getInt(String name) {
        Object v = get(name);
        if (v == null) {
            return null;
        }
        if (v instanceof Integer) {
            return (Integer) v;
        }
        return Integer.parseInt(v.toString());
    }

    /**
     * 根据索引获取一个整型
     *
     * @param index 索引
     * @return 整型或null
     */
    public Integer getInt(int index) {
        return getInt(getName(index));
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
     * @param name 名字
     * @return 双精度数字
     */
    public Double getDouble(String name) {
        Object v = get(name);
        if (v == null) {
            return null;
        }
        if (v instanceof Double) {
            return (Double) v;
        }
        return Double.parseDouble(v.toString());
    }

    /**
     * 获取一个双精度类型数组
     *
     * @param index 索引
     * @return 双精度数字或null
     */
    public Double getDouble(int index) {
        return getDouble(getName(index));
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
     * 获取一个可空双精度类型数组
     *
     * @param index 索引
     * @return 可空双精度数字
     */
    public Optional<Double> getOptionalDouble(int index) {
        return Optional.ofNullable(getDouble(index));
    }

    /**
     * 获取一个长整型值
     *
     * @param name 名字
     * @return 双精度数字
     */
    public Long getLong(String name) {
        Object v = get(name);
        if (v == null) {
            return null;
        }
        if (v instanceof Long) {
            return (Long) v;
        }
        return Long.parseLong(v.toString());
    }

    /**
     * 获取一个长整型值
     *
     * @param index 索引
     * @return 双精度数字
     */
    public Long getLong(int index) {
        return getLong(getName(index));
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
     * 获取一个可空长整型值
     *
     * @param name 名字
     * @return 可空长整型值
     */
    public Optional<Long> getOptionalLong(String name) {
        return Optional.ofNullable(getLong(name));
    }

    /**
     * 链式添加一个键值对，如果存在则进行覆盖
     *
     * @param name  字段名
     * @param value 值
     * @return 一个新的数据行
     */
    public DataRow add(String name, Object value) {
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
        DataRow res = empty().add(name, get(name));
        if (more.length > 0) {
            for (String n : more) {
                res.put(n, get(n));
            }
        }
        return res;
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
        for (String key : keySet()) {
            map.put(key, mapper.apply(get(key)));
        }
        return map;
    }

    /**
     * 转为Map
     *
     * @return map
     */
    public Map<String, Object> toMap() {
        return this;
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
        return obj2Json(this);
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
            return empty();
        }
        if (rows.size() == 1) {
            return rows.iterator().next();
        }
        boolean first = true;
        DataRow res = new DataRow();
        Set<String> names = null;
        for (DataRow row : rows) {
            if (first) {
                names = row.keySet();
                for (String key : row.keySet()) {
                    res.put(key, new ArrayList<>());
                }
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
            DataRow row = new DataRow();
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
                        row.put(field, value);
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
    public static DataRow fromMap(Map<String, ?> map) {
        return new DataRow(map);
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
        DataRow row = new DataRow();
        int len = pairs.length >> 1;
        for (int i = 0; i < len; i++) {
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
        return (DataRow) json2Obj(json, DataRow.class);
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
        for (String key : keySet()) {
            acc = mapper.apply(acc, key, get(key));
        }
        return acc;
    }
}
