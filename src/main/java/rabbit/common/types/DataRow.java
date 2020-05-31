package rabbit.common.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 行数据类型
 */
public final class DataRow {
    private final String[] names;
    private final String[] types;
    private final Object[] values;
    private List<String> _names;
    private List<String> _types;

    private DataRow(String[] names, String[] types, Object[] values) {
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
        return new DataRow(names, types, values);
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
            types[i] = values[i].getClass().getName();
        }
        return of(names, types, values);
    }

    /**
     * @return 一个空的DataRow
     */
    public static DataRow empty() {
        return new DataRow(new String[0], new String[0], new Object[0]);
    }

    /**
     * 判断是否为空
     *
     * @return 是否为空
     */
    public boolean isEmpty() {
        return names == null || names.length == 0;
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
        if (_names == null)
            _names = Arrays.asList(names);
        return _names;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public List<String> getTypes() {
        if (_types == null)
            _types = Arrays.asList(types);
        return _types;
    }

    /**
     * 根据字段名获取类型
     *
     * @param name 字段
     * @return 类型
     */
    public String getType(String name) {
        int index = getNames().indexOf(name);
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
     * 获取值
     *
     * @param name 名称
     * @param <T>  类型参数
     * @return 值
     */
    public <T> T get(String name) {
        int index = getNames().indexOf(name);
        return get(index);
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
     * 根据名字获取一个字符串
     *
     * @param name 索引
     * @return 字符串或null
     */
    public String getString(String name) {
        int index = getNames().indexOf(name);
        return getString(index);
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
        String v = value.toString();
        return Integer.valueOf(v);
    }

    /**
     * 根据名字获取一个整型
     *
     * @param name 名字
     * @return 整型或null
     */
    public Integer getInt(String name) {
        int index = getNames().indexOf(name);
        return getInt(index);
    }

    /**
     * 转换为Map
     *
     * @param valueConvert 值转换器
     * @param <T>          值类型参数
     * @return 一个Map
     */
    public <T> Map<String, T> toMap(Function<Object, T> valueConvert) {
        return getNames().stream().collect(
                HashMap::new,
                (current, k) -> current.put(k, valueConvert.apply(get(k))),
                HashMap::putAll
        );
    }

    /**
     * 转为Map
     *
     * @return map
     */
    public Map<String, Object> toMap() {
        return toMap(v -> v);
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
