package rabbit.common.io;

import java.util.Properties;

public class TypedProperties extends Properties {
    public int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }

    public int getInt(String key, int defaultValue) {
        if (containsKey(key)) {
            return getInt(key);
        }
        return defaultValue;
    }

    public boolean getBool(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

    public boolean getBool(String key, boolean defaultValue) {
        if (containsKey(key)) {
            return getBool(key);
        }
        return defaultValue;
    }

    public long getLong(String key) {
        return Long.parseLong(getProperty(key));
    }

    public long getLong(String key, long defaultValue) {
        if (containsKey(key)) {
            return Long.parseLong(getProperty(key));
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return Double.parseDouble(getProperty(key));
    }

    public double getDouble(String key, double defaultValue) {
        if (containsKey(key)) {
            return Double.parseDouble(getProperty(key));
        }
        return defaultValue;
    }

    public float getFloat(String key) {
        return Float.parseFloat(getProperty(key));
    }

    public float getFloat(String key, float defaultValue) {
        if (containsKey(key)) {
            return Float.parseFloat(getProperty(key));
        }
        return defaultValue;
    }
}
