package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.Jackson;
import com.github.chengyuxing.common.utils.ObjectUtil;

import java.util.*;
import java.util.function.Function;

import static com.github.chengyuxing.common.utils.ObjectUtil.coalesce;

/**
 * A useful data type with more feature which extends LinkedHashMap.
 */
public final class DataRow extends LinkedHashMap<String, Object> implements MapExtends<Object> {
    /**
     * Constructed a new DataRow
     */
    public DataRow() {
    }

    /**
     * Constructed a new DataRow with initial map.
     *
     * @param map initial map
     */
    public DataRow(Map<String, Object> map) {
        super(map);
    }

    /**
     * Constructed a new DataRow with initial capacity.
     *
     * @param capacity initial capacity
     */
    public DataRow(int capacity) {
        super(capacity);
    }

    /**
     * Constructed a new DataRow from pairs.
     *
     * @param input key-value pairs: k v，k v...
     * @return DataRow instance
     */
    public static DataRow of(Object... input) {
        return ObjectUtil.pairs2map(DataRow::new, input);
    }

    /**
     * Constructed a new DataRow from keys array and values array.
     *
     * @param keys   keys array
     * @param values values array
     * @return DataRow instance
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
     * Constructed a new DataRow from json.
     *
     * @param json json object e.g. {@code {"a":1,"b":2}}
     * @return DataRow instance
     */
    public static DataRow ofJson(String json) {
        if (Objects.isNull(json)) return new DataRow(0);
        return Jackson.toObject(json, DataRow.class);
    }

    /**
     * Constructed a new DataRow from standard java bean entity.
     *
     * @param entity entity
     * @return DataRow instance
     */
    public static DataRow ofEntity(Object entity) {
        return ObjectUtil.entity2map(entity, DataRow::new);
    }

    /**
     * Constructed a new DataRow from map.
     *
     * @param map map
     * @return DataRow instance
     */
    public static DataRow ofMap(Map<String, Object> map) {
        if (Objects.isNull(map)) return new DataRow(0);
        return new DataRow(map);
    }

    /**
     * Convert collection of map's rows to columns structure.
     *
     * @param rows collection of map
     * @return columns struct data
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
     * Get names.
     *
     * @return name list
     */
    public List<String> names() {
        return new ArrayList<>(keySet());
    }

    /**
     * Get value by index.
     *
     * @param index index
     * @return value
     * @throws IndexOutOfBoundsException if index out of range
     */
    private Object _getByIndex(int index) {
        Object[] values = values().toArray();
        Object v = values[index];
        Arrays.fill(values, null);
        return v;
    }

    /**
     * Get first value.
     *
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Object getFirst(Object... defaults) {
        if (isEmpty()) {
            return coalesce(defaults);
        }
        Object v = values().iterator().next();
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Convert first value and get.
     *
     * @param defaults default values, detect get first non-null value
     * @param <T>      result type
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getFirstAs(T... defaults) {
        return (T) getFirst((Object[]) defaults);
    }

    /**
     * Convert value and get by name.
     *
     * @param name     key
     * @param defaults default values, detect get first non-null value
     * @param <T>      result type
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(String name, T... defaults) {
        T v = (T) get(name);
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Convert value and get by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @param <T>      result type
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(int index, T... defaults) {
        T v = (T) _getByIndex(index);
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Get optional value by name.
     *
     * @param name key
     * @return optional value
     */
    public Optional<Object> getOptional(String name) {
        return Optional.ofNullable(getAs(name));
    }

    /**
     * Get optional value by index.
     *
     * @param index index
     * @return optional value
     * @throws IndexOutOfBoundsException if index out of range
     */
    public Optional<Object> getOptional(int index) {
        return Optional.ofNullable(getAs(index));
    }

    /**
     * Get string value by name.
     *
     * @param name     key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public String getString(String name, String... defaults) {
        Object v = get(name);
        return Objects.nonNull(v) ? v.toString() : coalesce(defaults);
    }

    /**
     * Get string value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     * @throws IndexOutOfBoundsException if index out of range
     */
    public String getString(int index, String... defaults) {
        Object v = _getByIndex(index);
        return Objects.nonNull(v) ? v.toString() : coalesce(defaults);
    }

    /**
     * Get int value by name.
     *
     * @param name     key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Integer getInt(String name, Integer... defaults) {
        Integer v = ObjectUtil.toInteger(get(name));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Get int value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     * @throws IndexOutOfBoundsException if index out of range
     */
    public Integer getInt(int index, Integer... defaults) {
        Integer v = ObjectUtil.toInteger(_getByIndex(index));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Get double value by name.
     *
     * @param name     key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Double getDouble(String name, Double... defaults) {
        Double v = ObjectUtil.toDouble(get(name));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Get double value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     * @throws IndexOutOfBoundsException if index out of range
     */
    public Double getDouble(int index, Double... defaults) {
        Double v = ObjectUtil.toDouble(_getByIndex(index));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Get long value by name.
     *
     * @param name     key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Long getLong(String name, Long... defaults) {
        Long v = ObjectUtil.toLong(get(name));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Get long value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     * @throws IndexOutOfBoundsException if index out of range
     */
    public Long getLong(int index, Long... defaults) {
        Long v = ObjectUtil.toLong(_getByIndex(index));
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Get value type class by name.
     *
     * @param name key
     * @return type class or null
     */
    public Class<?> getType(String name) {
        Object v = get(name);
        if (Objects.nonNull(v)) {
            return v.getClass();
        }
        return null;
    }

    /**
     * Get value type class by name.
     *
     * @param index index
     * @return type class or null
     * @throws IndexOutOfBoundsException if index out of range
     */
    public Class<?> getType(int index) {
        Object v = _getByIndex(index);
        if (Objects.nonNull(v)) {
            return v.getClass();
        }
        return null;
    }

    /**
     * Add a key-value.
     *
     * @param key   key
     * @param value value
     * @return DataRow
     */
    public DataRow add(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * Update a value.
     *
     * @param key     key
     * @param updater updater: old value {@code ->} new value
     * @param <T>     类型参数
     * @return true if exists &amp; updated or false
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
     * Pick some names to create a new DataRow.
     *
     * @param name key
     * @param more more keys
     * @return new DataRow instance
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
     * Reduce.
     *
     * @param init   initial accumulator value
     * @param mapper (accumulator, key, value) {@code ->} accumulator
     * @param <T>    result type
     * @return any type result
     */
    public <T> T reduce(T init, TiFunction<T, String, Object, T> mapper) {
        T acc = init;
        for (Map.Entry<String, Object> e : entrySet()) {
            acc = mapper.apply(acc, e.getKey(), e.getValue());
        }
        return acc;
    }

    /**
     * Convert to standard java bean entity.
     *
     * @param clazz                 entity class
     * @param constructorParameters constructor's parameters is required if entity class only have
     *                              1 constructor with parameters
     *                              e.g.
     *                              <blockquote>
     *                              <pre>DataRow row = DataRow.of("x", 2, "y", 5, ...);</pre>
     *                              <pre>row.toEntity(A.class, row.get("x"), row.get("y"));</pre>
     *                              </blockquote>
     * @param <T>                   entity class type
     * @return entity
     */
    public <T> T toEntity(Class<T> clazz, Object... constructorParameters) {
        return ObjectUtil.map2entity(this, clazz, constructorParameters);
    }

    /**
     * Convert to new LinkedHashMap.
     *
     * @return new LinkedHashMap
     */
    public Map<String, Object> toMap() {
        return new LinkedHashMap<>(this);
    }

    /**
     * Convert to json.
     *
     * @return json
     */
    public String toJson() {
        if (this.isEmpty()) return "{}";
        return Jackson.toJson(this);
    }

    /**
     * Convert to any.
     *
     * @param converter DataRow {@code ->} any
     * @param <T>       result type
     * @return any result
     */
    public <T> T to(Function<DataRow, T> converter) {
        return converter.apply(this);
    }
}

