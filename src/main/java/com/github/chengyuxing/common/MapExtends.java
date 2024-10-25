package com.github.chengyuxing.common;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Map interface extends support some useful method.
 *
 * @param <SELF> the type of keys maintained by this map
 * @param <V>    value type
 */
public interface MapExtends<SELF extends MapExtends<SELF, V>, V> extends Map<String, V> {
    /**
     * Remove elements which value is null.
     *
     * @return SELF
     */
    default SELF removeIfAbsent() {
        entrySet().removeIf(stringVEntry -> stringVEntry.getValue() == null);
        //noinspection unchecked
        return (SELF) this;
    }

    /**
     * Remove elements which value is null but excludes special keys.
     *
     * @param keys excluded keys
     * @return SELF
     */
    default SELF removeIfAbsentExclude(String... keys) {
        entrySet().removeIf(e -> e.getValue() == null && !Arrays.asList(keys).contains(e.getKey()));
        //noinspection unchecked
        return (SELF) this;
    }

    /**
     * Remove element by predicate.
     *
     * @param predicate predicate
     * @return SELF
     */
    default SELF removeIf(BiPredicate<String, V> predicate) {
        entrySet().removeIf(next -> predicate.test(next.getKey(), next.getValue()));
        //noinspection unchecked
        return (SELF) this;
    }

    /**
     * Add a key-value.
     *
     * @param key   key
     * @param value value
     * @return SELF
     */
    default SELF add(String key, V value) {
        put(key, value);
        //noinspection unchecked
        return (SELF) this;
    }

    /**
     * Update a key name to another.
     *
     * @param oldKey old key name
     * @param newKey new key name
     * @return SELF
     */
    default SELF updateKey(String oldKey, String newKey) {
        if (containsKey(oldKey)) {
            put(newKey, remove(oldKey));
        }
        //noinspection unchecked
        return (SELF) this;
    }

    /**
     * Update all key names.
     *
     * @param updater (old key name) -&gt; (new key name)
     * @return SELF
     * @throws IllegalStateException new key exists
     */
    default SELF updateKeys(Function<String, String> updater) {
        for (String key : keySet().toArray(new String[0])) {
            String newKey = updater.apply(key);
            if (containsKey(newKey)) {
                throw new IllegalStateException("New key " + newKey + " already exists");
            }
            put(newKey, remove(key));
        }
        //noinspection unchecked
        return (SELF) this;
    }

    /**
     * Update a value.
     *
     * @param key     key
     * @param updater updater: old value -&gt; new value
     * @return SELF
     */
    default SELF updateValue(String key, Function<V, V> updater) {
        V currentValue = get(key);
        if (Objects.nonNull(currentValue) || containsKey(key)) {
            put(key, updater.apply(get(key)));
        }
        //noinspection unchecked
        return (SELF) this;
    }

    /**
     * Update all values.
     *
     * @param updater (key ,old value) -&gt; (new value)
     * @return SELF
     */
    default SELF updateValues(BiFunction<String, V, V> updater) {
        for (Entry<String, V> entry : entrySet()) {
            entry.setValue(updater.apply(entry.getKey(), entry.getValue()));
        }
        //noinspection unchecked
        return (SELF) this;
    }
}
