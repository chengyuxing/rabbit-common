package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.Jackson;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.*;
import java.util.function.Function;

import static com.github.chengyuxing.common.utils.ObjectUtil.coalesce;

/**
 * 对LinkedHashMap进行一些扩展的数据行对象
 */
public final class DataRow extends LinkedHashMap<String, Object> implements MapExtends<Object> {
    /**
     * 一个空的DataRow
     */
    public DataRow() {
    }

    /**
     * 一个具有初始值的DataRow
     *
     * @param map 初始map
     */
    public DataRow(Map<String, Object> map) {
        super(map);
    }

    /**
     * 一个空的DataRow
     *
     * @param capacity 初始容量大小
     */
    public DataRow(int capacity) {
        super(capacity);
    }

    /**
     * 从一组键值对创建一个DataRow
     *
     * @param input 键值对 k v，k v...
     * @return DataRow
     */
    public static DataRow of(Object... input) {
        return ObjectUtil.pairs2map(DataRow::new, input);
    }

    /**
     * 新建一个DataRow
     *
     * @param keys   一组字段名
     * @param values 一组值
     * @return 新实例，初始化小为字段名数组的长度
     */
    public static DataRow of(String[] keys, Object[] values) {
        if (keys.length == values.length) {
            if (keys.length == 0) {
                return new DataRow(0);
            }
            DataRow row = new DataRow(keys.length);
            for (int i = 0; i < keys.length; i++) {
                row.put(keys[i], values[i]);
            }
            return row;
        }
        throw new IllegalArgumentException("names and values length not equal!");
    }

    /**
     * 从一个json对象字符串创建一个DataRow
     *
     * @param json json对象字符串 e.g. {@code {"a":1,"b":2}}
     * @return DataRow
     */
    public static DataRow ofJson(String json) {
        if (Objects.isNull(json)) return new DataRow(0);
        return Jackson.toObject(json, DataRow.class);
    }

    /**
     * 从一个标准的javaBean实体转为DataRow类型
     *
     * @param entity 实体
     * @return DataRow
     */
    public static DataRow ofEntity(Object entity) {
        return ObjectUtil.entity2map(entity, DataRow::new);
    }

    /**
     * 从map转换到DataRow
     *
     * @param map map
     * @return DataRow
     */
    public static DataRow ofMap(Map<String, Object> map) {
        if (Objects.isNull(map)) return new DataRow(0);
        return new DataRow(map);
    }

    /**
     * 组合多个DataRow以创建一个DataRow（数据行转为列）
     *
     * @param rows 数据行集合 （为保证正确性，默认以第一行字段为列名，每行字段都必须相同）
     * @return 一行以列存储形式的数据行
     */
    public static DataRow zip(Collection<? extends Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return new DataRow(0);
        }
        boolean first = true;
        DataRow res = null;
        Set<String> names = null;
        for (Map<String, Object> row : rows) {
            if (first) {
                res = new DataRow(row.size());
                names = row.keySet();
                for (String name : names) {
                    res.put(name, new ArrayList<>());
                }
                first = false;
            }
            for (String name : names) {
                //noinspection unchecked
                ((List<Object>) res.get(name)).add(row.get(name));
            }
        }
        return res;
    }

    /**
     * 获取字段名集合
     *
     * @return 字段名集合
     */
    public List<String> names() {
        return new ArrayList<>(keySet());
    }

    /**
     * 根据索引获取值
     *
     * @param index 索引
     * @return 值
     * @throws IndexOutOfBoundsException 如果索引超出界限
     */
    private Object _getByIndex(int index) {
        Object[] values = values().toArray();
        Object v = values[index];
        Arrays.fill(values, null);
        return v;
    }

    /**
     * 获取第一个值
     *
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 第一个值或null
     */
    public Object getFirst(Object... defaults) {
        if (isEmpty()) {
            return coalesce(defaults);
        }
        Object v = values().iterator().next();
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 隐式转换获取第一个值
     *
     * @param defaults 更多默认值，选定第一个不为null的值
     * @param <T>      结果类型参数
     * @return 第一个值或null
     */
    @SuppressWarnings("unchecked")
    public <T> T getFirstAs(T... defaults) {
        return (T) getFirst((Object[]) defaults);
    }

    /**
     * 隐式转换获取一个值
     *
     * @param key      键
     * @param defaults 更多默认值，选定第一个不为null的值
     * @param <T>      结果类型参数
     * @return 值或null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(String key, T... defaults) {
        T v = (T) get(key);
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 隐式转换获取一个值
     *
     * @param index    索引
     * @param defaults 更多默认值，选定第一个不为null的值
     * @param <T>      结果类型参数
     * @return 值或null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(int index, T... defaults) {
        T v = (T) _getByIndex(index);
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 根据名字获取可空值
     *
     * @param name 名字
     * @return 可空值
     */
    public Optional<Object> getOptional(String name) {
        return Optional.ofNullable(getAs(name));
    }

    /**
     * 根据索引获取可空值
     *
     * @param index 索引
     * @return 可空值
     * @throws IndexOutOfBoundsException 如果索引超出界限
     */
    public Optional<Object> getOptional(int index) {
        return Optional.ofNullable(getAs(index));
    }

    /**
     * 根据名字获取一个字符串
     *
     * @param name     名字
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 字符串或null
     */
    public String getString(String name, String... defaults) {
        Object v = get(name);
        return Objects.nonNull(v) ? v.toString() : coalesce(defaults);
    }

    /**
     * 根据索引获取一个字符串
     *
     * @param index    索引
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 字符串或null
     * @throws IndexOutOfBoundsException 如果索引超出界限
     */
    public String getString(int index, String... defaults) {
        Object v = _getByIndex(index);
        return Objects.nonNull(v) ? v.toString() : coalesce(defaults);
    }

    /**
     * 根据名字获取一个整型
     *
     * @param name     名字
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 整型或null
     */
    public Integer getInt(String name, Integer... defaults) {
        Integer v = ObjectUtil.toInteger(get(name));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 根据索引获取一个整型
     *
     * @param index    索引
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 整型或null
     * @throws IndexOutOfBoundsException 如果索引超出界限
     */
    public Integer getInt(int index, Integer... defaults) {
        Integer v = ObjectUtil.toInteger(_getByIndex(index));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 根据索引获取一个双精度类型数组
     *
     * @param name     键名
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 双精度数字或null
     */
    public Double getDouble(String name, Double... defaults) {
        Double v = ObjectUtil.toDouble(get(name));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 根据索引获取一个双精度类型数组
     *
     * @param index    索引
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 双精度数字或null
     * @throws IndexOutOfBoundsException 如果索引超出界限
     */
    public Double getDouble(int index, Double... defaults) {
        Double v = ObjectUtil.toDouble(_getByIndex(index));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 获取一个长整型值
     *
     * @param name     键名
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 双精度数字或null
     */
    public Long getLong(String name, Long... defaults) {
        Long v = ObjectUtil.toLong(get(name));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 根据索引获取一个长整型值
     *
     * @param index    索引
     * @param defaults 更多默认值，选定第一个不为null的值
     * @return 双精度数字或null
     * @throws IndexOutOfBoundsException 如果索引超出界限
     */
    public Long getLong(int index, Long... defaults) {
        Long v = ObjectUtil.toLong(_getByIndex(index));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * 根据字段名获取类型
     *
     * @param name 字段
     * @return 如果值存在或不为null返回值类型名称，否则返回null
     */
    public Class<?> getType(String name) {
        Object v = get(name);
        if (Objects.nonNull(v)) {
            return v.getClass();
        }
        return null;
    }

    /**
     * 根据字段名获取类型
     *
     * @param index 索引
     * @return 如果值存在或不为null返回值类型名称，否则返回null
     * @throws IndexOutOfBoundsException 如果索引超出界限
     */
    public Class<?> getType(int index) {
        Object v = _getByIndex(index);
        if (Objects.nonNull(v)) {
            return v.getClass();
        }
        return null;
    }

    /**
     * 链式添加一个键值对
     *
     * @param key   键名
     * @param value 值
     * @return 对象自身
     */
    public DataRow add(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * 更新一个值
     *
     * @param key     键名
     * @param updater 更新器
     * @param <T>     类型参数
     * @return 是否存在并更新
     */
    @SuppressWarnings("unchecked")
    public <T> boolean update(String key, Function<T, Object> updater) {
        if (containsKey(key)) {
            Object oldV = get(key);
            Object newV = updater.apply((T) oldV);
            put(key, newV);
            return true;
        }
        return false;
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
        row.put(name, get(name));
        for (String n : more) {
            row.put(n, get(n));
        }
        return row;
    }

    /**
     * 归并操作
     *
     * @param init   初始值
     * @param mapper 映射(初始值，名字，值)
     * @param <T>    类型参数
     * @return 归并后的结果
     */
    public <T> T reduce(T init, TiFunction<T, String, Object, T> mapper) {
        T acc = init;
        for (Map.Entry<String, Object> e : entrySet()) {
            acc = mapper.apply(acc, e.getKey(), e.getValue());
        }
        return acc;
    }

    /**
     * 转为一个标准的javaBean实体
     *
     * @param clazz                 实体类
     * @param constructorParameters 如果实体类只有一个带参数的构造函数，则指定参数<br>
     *                              e.g.
     *                              <blockquote>
     *                              <pre>DataRow row = DataRow.fromPair("x", 2, "y", 5, ...);</pre>
     *                              <pre>row.toEntity(A.class, row.get("x"), row.get("y"));</pre>
     *                              </blockquote>
     * @param <T>                   结果类型参数
     * @return 实体
     */
    public <T> T toEntity(Class<T> clazz, Object... constructorParameters) {
        return ObjectUtil.map2entity(this, clazz, constructorParameters);
    }

    /**
     * 转为一个新的map
     *
     * @return 新的LinkedHashMap
     */
    public Map<String, Object> toMap() {
        return new LinkedHashMap<>(this);
    }

    /**
     * 转为一个json字符串
     *
     * @return json字符串
     */
    public String toJson() {
        if (this.isEmpty()) return "{}";
        return Jackson.toJson(this);
    }

    /**
     * 转换DataRow
     *
     * @param converter 转换器
     * @param <T>       类型参数
     * @return 转换后的实例
     */
    public <T> T to(Function<DataRow, T> converter) {
        return converter.apply(this);
    }
}

