package rabbit.common.types;

import java.util.*;
import java.util.function.Function;

/**
 * 行数据类型
 */
public final class DataRow {
    private final String[] names;
    private final String[] types;
    private final Object[] values;

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
        if (names.length == types.length && types.length == values.length) {
            return new DataRow(names, types, values);
        }
        throw new IllegalArgumentException("all of 3 args's length not equal!");
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
     * 获取可为空的值
     *
     * @param index 索引
     * @param <T>   类型参数
     * @return 值
     */
    public <T> Optional<T> getNullable(int index) {
        return Optional.ofNullable(get(index));
    }

    /**
     * 获取可为空的值
     *
     * @param name 名称
     * @param <T>  类型参数
     * @return 值
     */
    public <T> Optional<T> getNullable(String name) {
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
     * 根据名字获取一个字符串
     *
     * @param name 索引
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
     * 获取一个双精度类型数组
     *
     * @param index 索引
     * @return 双精度数字
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
     * 从List转换到DataRow
     *
     * @param list  数据
     * @param names 列字段名
     * @return DataRow
     */
    public static DataRow fromList(List<?> list, String... names) {
        String[] types = new String[names.length];
        Object[] values = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            Object value = list.get(i);
            values[i] = value;
            if (value == null) {
                types[i] = "null";
            } else {
                types[i] = value.getClass().getName();
            }
        }
        return of(names, types, values);
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
