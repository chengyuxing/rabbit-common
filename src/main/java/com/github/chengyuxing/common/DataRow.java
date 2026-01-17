package com.github.chengyuxing.common;

import com.github.chengyuxing.common.util.ValueUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.chengyuxing.common.util.ValueUtils.coalesce;

/**
 * Represents a row of data, similar to a database table row, with key-value pairs.
 * <p>
 * Provides various methods for creating, manipulating, and converting DataRow instances.
 */
public class DataRow extends LinkedHashMap<String, Object> implements MapExtends<DataRow, Object> {
    /**
     * Constructs a new empty DataRow.
     */
    public DataRow() {
    }

    /**
     * Constructs a new DataRow with initial capacity.
     *
     * @param capacity initial capacity
     */
    public DataRow(int capacity) {
        super(capacity);
    }

    /**
     * Constructs a new DataRow with map.
     *
     * @param map map
     */
    public DataRow(@NotNull Map<String, Object> map) {
        super(map);
    }

    /**
     * Returns a new DataRow from pairs.
     *
     * @param input key-value pairs: k v，k v...
     * @return DataRow instance
     */
    public static DataRow of(Object... input) {
        return ValueUtils.pairsToMap(DataRow::new, input);
    }

    /**
     * Returns a new DataRow from keys array and values array.
     *
     * @param keys   keys array
     * @param values values array
     * @return DataRow instance
     */
    public static @NotNull DataRow of(String @NotNull [] keys, Object @NotNull [] values) {
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
        throw new IllegalArgumentException("keys and values length not equal!");
    }

    /**
     * Returns a new DataRow from standard java bean entity.
     *
     * @param entity entity
     * @return DataRow instance
     */
    public static DataRow ofEntity(Object entity) {
        return ValueUtils.entityToMap(entity, DataRow::new);
    }

    /**
     * Returns a new DataRow from standard java bean entity.
     *
     * @param entity      entity
     * @param fieldMapper entity field mapping to map's key
     * @return DataRow instance
     */
    public static DataRow ofEntity(Object entity, @NotNull Function<Field, String> fieldMapper) {
        return ValueUtils.entityToMap(entity, fieldMapper, DataRow::new);
    }

    /**
     * Returns a new DataRow from map.
     *
     * @param map map
     * @return DataRow instance
     */
    @Contract("_ -> new")
    public static @NotNull DataRow ofMap(@NotNull Map<String, Object> map) {
        return new DataRow(map);
    }

    /**
     * Convert collection of map's rows to columns structure.
     *
     * @param rows collection of map
     * @return columns struct data
     */
    public static @NotNull DataRow zip(@NotNull Collection<? extends Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return new DataRow(0);
        }
        Set<String> keys = rows.iterator().next().keySet();
        DataRow res = new DataRow(keys.size());
        for (String key : keys) {
            res.put(key, new ArrayList<>());
        }
        for (Map<String, Object> row : rows) {
            for (String key : keys) {
                //noinspection unchecked
                ((List<Object>) res.get(key)).add(row.get(key));
            }
        }
        return res;
    }

    /**
     * Get keys.
     *
     * @return key list
     */
    public List<String> names() {
        return new ArrayList<>(keySet());
    }

    /**
     * Get value by index.
     *
     * @param index index
     * @return value or null
     */
    protected Object getByIndex(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + size());
        }
        Iterator<Map.Entry<String, Object>> it = entrySet().iterator();
        for (int i = 0; i < index; i++) {
            it.next();
        }
        return it.next().getValue();
    }

    /**
     * Get first value.
     *
     * @param defaults default values, detect get first non-null value
     * @return the type of the value
     */
    public Object getFirst(Object... defaults) {
        if (isEmpty()) {
            return coalesce(defaults);
        }
        Object v = getByIndex(0);
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get first value and cast to type {@code T}.
     *
     * @param defaults default values, detect get first non-null value
     * @param <T>      the type of the value
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T> T getFirstAs(T... defaults) {
        return (T) getFirst((Object[]) defaults);
    }

    /**
     * Get first value and applies a function to transform
     * the value to type {@code T}.
     *
     * @param transformer a function that takes the retrieved object and returns a transformed value of type {@code T}
     * @param <T>         the type of the transformed value
     * @return the transformed value of type T after applying transformer to the retrieved value
     */
    public <T> T getFirstAs(@NotNull Function<Object, T> transformer) {
        return transformer.apply(getByIndex(0));
    }

    /**
     * Get value and cast to type {@code T} by key.
     *
     * @param key      key
     * @param defaults default values, detect get first non-null value
     * @param <T>      the type of the value
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T> T getAs(String key, T... defaults) {
        T v = (T) get(key);
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get value and applies a function to transform
     * * the value to type {@code T} by key.
     *
     * @param key         key
     * @param transformer a function that takes the retrieved object and returns a transformed value of type {@code T}
     * @param <T>         the type of the transformed value
     * @return the transformed value of type T after applying transformer to the retrieved value
     */
    public <T> T getAs(String key, @NotNull Function<Object, T> transformer) {
        return transformer.apply(get(key));
    }

    /**
     * Get value and cast to type {@code T} by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @param <T>      the type of the value
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T> T getAs(int index, T... defaults) {
        T v = (T) getByIndex(index);
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get value and applies a function to transform
     * * the value to type {@code T} by index.
     *
     * @param index       index
     * @param transformer a function that takes the retrieved object and returns a transformed value of type {@code T}
     * @param <T>         the type of the transformed value
     * @return the transformed value of type T after applying transformer to the retrieved value
     */
    public <T> T getAs(int index, @NotNull Function<Object, T> transformer) {
        return transformer.apply(getByIndex(index));
    }

    /**
     * Get deep nest object value and cast to type {@code T} by key path expression.
     * <p>
     * The method interprets the key as a path expression, the path is separated by '{@code .}', if
     * actual map keys contain '{@code .}', the behavior is undefined.
     * <blockquote><pre>
     *     {user: {age: 18, hobby: ["swim", "hiking", "sleep"]}}
     * </pre></blockquote>
     * <blockquote><pre>
     *     &lt;String&gt;deepGetAs("user.hobby.0"); // "swim"
     * </pre></blockquote>
     *
     * @param path     key path expression
     * @param defaults default values, detect get first non-null value
     * @param <T>      the type of the value
     * @return value or null
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T> T deepGetAs(@NotNull String path, T... defaults) {
        Object value;
        if (path.indexOf('.') >= 0) {
            value = ValueUtils.getDeepValue(this, path);
        } else {
            value = get(path);
        }
        T v = (T) value;
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get deep nest object value and applies a function to transform
     * the value to type {@code T} by key path expression.
     *
     * @param <T>         the type of the transformed value
     * @param path        key path expression
     * @param transformer a function that takes the retrieved object and returns a transformed value of type {@code T}
     * @return the transformed value of type T after applying transformer to the retrieved value
     * @see #deepGetAs(String, Object[])
     */
    public <T> T deepGetAs(@NotNull String path, @NotNull Function<Object, T> transformer) {
        Object value;
        if (path.indexOf('.') >= 0) {
            value = ValueUtils.getDeepValue(this, path);
        } else {
            value = get(path);
        }
        return transformer.apply(value);
    }

    /**
     * Get optional value and cast to type {@code T} by key.
     *
     * @param key key
     * @return value or null
     */
    public Optional<Object> getOptional(String key) {
        return Optional.ofNullable(get(key));
    }

    /**
     * Get optional value and cast to type {@code T} by index.
     *
     * @param index index
     * @return optional value
     */
    public Optional<Object> getOptional(int index) {
        return Optional.ofNullable(getByIndex(index));
    }

    /**
     * Get string value by key.
     *
     * @param key      key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public String getString(String key, String... defaults) {
        return Objects.toString(get(key), coalesce(defaults));
    }

    /**
     * Get string value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public String getString(int index, String... defaults) {
        return Objects.toString(getByIndex(index), coalesce(defaults));
    }

    /**
     * Get int value by key.
     *
     * @param key      key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Integer getInt(String key, Integer... defaults) {
        Integer v = ValueUtils.toInteger(get(key));
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get int value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Integer getInt(int index, Integer... defaults) {
        Integer v = ValueUtils.toInteger(getByIndex(index));
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get double value by key.
     *
     * @param key      key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Double getDouble(String key, Double... defaults) {
        Double v = ValueUtils.toDouble(get(key));
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get double value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Double getDouble(int index, Double... defaults) {
        Double v = ValueUtils.toDouble(getByIndex(index));
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get long value by key.
     *
     * @param key      key
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Long getLong(String key, Long... defaults) {
        Long v = ValueUtils.toLong(get(key));
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Get long value by index.
     *
     * @param index    index
     * @param defaults default values, detect get first non-null value
     * @return value or null
     */
    public Long getLong(int index, Long... defaults) {
        Long v = ValueUtils.toLong(getByIndex(index));
        return v != null ? v : coalesce(defaults);
    }

    /**
     * Pick some keys to create a new DataRow.
     *
     * @param key  key
     * @param more more keys
     * @return new DataRow instance
     */
    public DataRow pick(String key, String @NotNull ... more) {
        DataRow row = new DataRow(more.length + 1);
        row.put(key, get(key));
        for (String n : more) {
            row.put(n, get(n));
        }
        return row;
    }

    /**
     * Applies a function to each entry of the map, reducing the entries to a single value.
     * The reduction is performed by applying the given function to an accumulator and each
     * map entry (key and value), updating the accumulator with the result.
     *
     * @param <T>    the type of the accumulator and the return type
     * @param init   the initial value for the accumulator
     * @param mapper a function that takes three arguments: the current accumulator value,
     *               the key, and the value of the current entry, and returns the new
     *               accumulator value
     * @return the reduced value after applying the function to all entries
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
     * @param constructorParameters <p>constructor's parameters is required if entity class only have
     *                              1 constructor with parameters
     *                              e.g.</p>
     *                              <blockquote>
     *                              <pre>DataRow row = DataRow.of("x", 2, "y", 5, ...);</pre>
     *                              <pre>row.toEntity(A.class, row.get("x"), row.get("y"));</pre>
     *                              </blockquote>
     * @param <T>                   entity class type
     * @return entity
     */
    public <T> T toEntity(@NotNull Class<T> clazz, Object... constructorParameters) {
        return ValueUtils.mapToEntity(this, clazz, constructorParameters);
    }

    /**
     * Convert to standard java bean entity.
     *
     * @param clazz                 entity class
     * @param fieldMapper           entity field mapping to map's key, e.g.
     *                              <blockquote>
     *                              <pre>f -&gt; f.getName().replaceAll("([A-Z])", "_$1").toLowerCase();</pre>
     *                              <pre>// userId -&gt; user_id</pre>
     *                              </blockquote>
     * @param valueMapper           map value mapping to entity field: (entity field, map value) -&gt; new value
     * @param constructorParameters <p>constructor's parameters are required if entity class only have
     *                              1 constructor with parameters,
     *                              e.g.</p>
     *                              <blockquote>
     *                              <pre>DataRow row = DataRow.of("x", 2, "y", 5, ...);</pre>
     *                              <pre>row.toEntity(A.class, row.get("x"), row.get("y"));</pre>
     *                              </blockquote>
     * @param <T>                   entity class type
     * @return entity
     */
    public <T> T toEntity(@NotNull Class<T> clazz, @NotNull Function<Field, String> fieldMapper, @Nullable BiFunction<Field, Object, Object> valueMapper, Object... constructorParameters) {
        return ValueUtils.mapToEntity(this, clazz, fieldMapper, valueMapper, constructorParameters);
    }

    /**
     * Convert each entry to key-value list.
     *
     * @return key-value list
     */
    public List<KeyValue> toKeyValue() {
        List<KeyValue> kvs = new ArrayList<>(size());
        for (Map.Entry<String, Object> e : entrySet()) {
            kvs.add(new KeyValue(e.getKey(), e.getValue()));
        }
        return kvs;
    }
}

