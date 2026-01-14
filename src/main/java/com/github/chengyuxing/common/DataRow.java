package com.github.chengyuxing.common;

import com.github.chengyuxing.common.utils.ObjectUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.chengyuxing.common.utils.ObjectUtil.coalesce;

/**
 * Represents a row of data, similar to a database table row, with key-value pairs.
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
        return ObjectUtil.pairsToMap(DataRow::new, input);
    }

    /**
     * Returns a new DataRow from keys array and values array.
     *
     * @param keys   keys array
     * @param values values array
     * @return DataRow instance
     */
    public static DataRow of(@NotNull String[] keys, @NotNull Object[] values) {
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
     * Returns a new DataRow from standard java bean entity.
     *
     * @param entity entity
     * @return DataRow instance
     */
    public static DataRow ofEntity(Object entity) {
        return ObjectUtil.entityToMap(entity, DataRow::new);
    }

    /**
     * Returns a new DataRow from standard java bean entity.
     *
     * @param entity      entity
     * @param fieldMapper entity field mapping to map's key
     * @return DataRow instance
     */
    public static DataRow ofEntity(Object entity, @NotNull Function<Field, String> fieldMapper) {
        return ObjectUtil.entityToMap(entity, fieldMapper, DataRow::new);
    }

    /**
     * Returns a new DataRow from map.
     *
     * @param map map
     * @return DataRow instance
     */
    public static DataRow ofMap(@NotNull Map<String, Object> map) {
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
        Set<String> names = rows.iterator().next().keySet();
        DataRow res = new DataRow(names.size());
        for (String name : names) {
            res.put(name, new ArrayList<>());
        }
        for (Map<String, Object> row : rows) {
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
     * @return value or null
     */
    private Object _getByIndex(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }
        Iterator<Map.Entry<String, Object>> it = entrySet().iterator();
        for (int i = 0; i < index; i++) {
            it.next();
        }
        return it.next().getValue();
    }

    /**
     * Retrieves the first element of the collection.
     * If the collection is empty, it returns the first non-null value from the provided defaults.
     * If no non-null default is found, it returns null.
     *
     * @param defaults variable number of arguments that serve as fallback values if the collection is empty
     * @return the first element of the collection or the first non-null value from the defaults if the collection is empty
     */
    public Object getFirst(Object... defaults) {
        if (isEmpty()) {
            return coalesce(defaults);
        }
        Object v = _getByIndex(0);
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Retrieves the first element from the provided varargs and casts it to the specified type.
     * If no elements are provided, it will return null.
     *
     * @param <T>      the type of the elements in the varargs
     * @param defaults the varargs of elements to retrieve the first from
     * @return the first element cast to the specified type, or null if the array is empty
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T> T getFirstAs(T... defaults) {
        return (T) getFirst((Object[]) defaults);
    }

    /**
     * Retrieves the value associated with the given name, or returns the first non-null default value if the key does not exist or its value is null.
     *
     * @param <T>      the type of the value to retrieve
     * @param name     the name of the value to be retrieved
     * @param defaults a variable number of arguments representing the default values
     * @return the value associated with the given name, or the first non-null value from the defaults if the original value is null or not found
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T> T getAs(String name, T... defaults) {
        T v = (T) get(name);
        return Objects.nonNull(v) ? v : coalesce(defaults);
    }

    /**
     * Retrieves a deeply nested value from the object based on the provided path.
     * If the value is not found, it returns the first non-null default value provided.
     *
     * @param <T>      The type of the value to retrieve.
     * @param path     The path to the value, using dot notation for nested properties.
     * @param defaults Variable length argument list of default values to return if the
     *                 requested value is null. The first non-null value in this list will
     *                 be returned.
     * @return The value found at the specified path, or the first non-null value from
     * the defaults if the path does not resolve to a non-null value.
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T> T deepGetAs(@NotNull String path, T... defaults) {
        if (!path.contains(".")) {
            return getAs(path, defaults);
        }
        T v = (T) ObjectUtil.getDeepValue(this, path);
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
    @SafeVarargs
    public final <T> T getAs(int index, T... defaults) {
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
     */
    public Long getLong(int index, Long... defaults) {
        Long v = ObjectUtil.toLong(_getByIndex(index));
        return Objects.nonNull(v) ? v : coalesce(defaults);
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
     * @param mapper (accumulator, key, value) -&gt; accumulator
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
        return ObjectUtil.mapToEntity(this, clazz, constructorParameters);
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
        return ObjectUtil.mapToEntity(this, clazz, fieldMapper, valueMapper, constructorParameters);
    }

    /**
     * Convert each entry to key value list.
     *
     * @return key value list
     */
    public List<KeyValue> toKeyValue() {
        List<KeyValue> kvs = new ArrayList<>(size());
        for (Map.Entry<String, Object> e : entrySet()) {
            kvs.add(new KeyValue(e.getKey(), e.getValue()));
        }
        return kvs;
    }
}

