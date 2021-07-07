package tests;

import com.github.chengyuxing.common.DateTimes;
import com.github.chengyuxing.common.utils.ReflectUtil;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Function;

import static com.github.chengyuxing.common.utils.ReflectUtil.json2Obj;

public class DataRow2 {
    private final Object[] grid;
    private static final int rows = 3;
    private final int columns;

    DataRow2(String[] names, String[] types, Object[] values) {
        if (names.length == types.length && types.length == values.length) {
            columns = names.length;
            grid = new Object[rows * columns];
            System.arraycopy(names, 0, grid, 0, columns);
            System.arraycopy(types, 0, grid, columns, columns);
            System.arraycopy(values, 0, grid, columns << 1, columns);
        } else {
            throw new IllegalArgumentException("all of 3 args length not equal!");
        }
    }

    public static DataRow2 of(String[] names, String[] types, Object[] values) {
        return new DataRow2(names, types, values);
    }

    public static DataRow2 of(String[] names, Object[] values) {
        String[] types = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value == null) {
                types[i] = "null";
            } else {
                types[i] = value.getClass().getName();
            }
        }
        return of(names, types, values);
    }

    private void checkIndexIsValid(int column) {
        if (column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException("Index out of range: " + column);
        }
    }

    public static DataRow2 empty() {
        return of(new String[0], new String[0], new Object[0]);
    }

    public boolean isEmpty() {
        return columns == 0;
    }

    public List<Object> getValues() {
        Object[] values = new Object[columns];
        System.arraycopy(grid, columns << 1, values, 0, columns);
        return Arrays.asList(values);
    }

    public List<String> getNames() {
        String[] names = new String[columns];
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(grid, 0, names, 0, columns);
        return Arrays.asList(names);
    }

    public List<String> getTypes() {
        String[] types = new String[columns];
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(grid, columns, types, 0, columns);
        return Arrays.asList(types);
    }

    public String getType(int index) {
        checkIndexIsValid(index);
        return grid[columns + index].toString();
    }

    public String getType(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getType(index);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int index) {
        checkIndexIsValid(index);
        return (T) grid[(columns << 1) + index];
    }

    public <T> T get(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return get(index);
    }

    public String getString(int index) {
        Object value = get(index);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public String getString(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getString(index);
    }

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

    public Integer getInt(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getInt(index);
    }

    public Double getDouble(String name) {
        int index = indexOf(name);
        if (index == -1) {
            return null;
        }
        return getDouble(index);
    }

    public int size() {
        return columns;
    }

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

    public <T> Optional<T> getNullable(int index) {
        return Optional.ofNullable(get(index));
    }

    public <T> Optional<T> getNullable(String name) {
        return Optional.ofNullable(get(name));
    }

    public int indexOf(String name) {
        int index = -1;
        if (name == null) {
            return index;
        }
        for (int i = 0; i < columns; i++) {
            if (grid[i].equals(name)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public <T> Map<String, T> toMap(Function<Object, T> valueConvert) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < columns; i++) {
            map.put(grid[i].toString(), valueConvert.apply(get(i)));
        }
        return map;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < columns; i++) {
            map.put(grid[i].toString(), get(i));
        }
        return map;
    }

    public <T> T toEntity(Class<T> clazz) {
        try {
            T entity = clazz.newInstance();
            for (Method method : ReflectUtil.getWRMethods(clazz).getItem2()) {
                if (method.getName().startsWith("set")) {
                    String field = method.getName().substring(3);
                    field = field.substring(0, 1).toLowerCase().concat(field.substring(1));
                    Object value = get(field);
                    String valueType = getType(field);
                    if (value != null && valueType != null) {
                        Class<?> argType = method.getParameterTypes()[0];
                        if (argType == Date.class) {
                            switch (valueType) {
                                case "java.sql.Time":
                                    method.invoke(entity, new Date(((Time) value).toLocalTime().atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                                    break;
                                case "java.sql.Timestamp":
                                    method.invoke(entity, new Date(((Timestamp) value).toInstant().toEpochMilli()));
                                    break;
                                case "java.sql.Date":
                                    method.invoke(entity, new Date(((java.sql.Date) value).toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()));
                                    break;
                                case "java.lang.String":
                                    method.invoke(entity, DateTimes.toDate(value.toString()));
                                    break;
                            }
                        } else if (Temporal.class.isAssignableFrom(argType)) {
                            switch (valueType) {
                                case "java.sql.Date":
                                    if (argType == LocalDate.class) {
                                        method.invoke(entity, ((java.sql.Date) value).toLocalDate());
                                    }
                                    break;
                                case "java.sql.Timestamp":
                                    if (argType == LocalDateTime.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime());
                                    } else if (argType == Instant.class) {
                                        method.invoke(entity, ((Timestamp) value).toInstant());
                                    } else if (argType == LocalDate.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime().toLocalDate());
                                    } else if (argType == LocalTime.class) {
                                        method.invoke(entity, ((Timestamp) value).toLocalDateTime().toLocalTime());
                                    }
                                    break;
                                case "java.sql.Time":
                                    if (argType == LocalTime.class) {
                                        method.invoke(entity, ((Time) value).toLocalTime());
                                    }
                                    break;
                                case "java.lang.String":
                                    if (argType == LocalDateTime.class) {
                                        method.invoke(entity, DateTimes.toLocalDateTime(value.toString()));
                                    } else if (argType == Instant.class) {
                                        method.invoke(entity, DateTimes.toInstant(value.toString()));
                                    } else if (argType == LocalDate.class) {
                                        method.invoke(entity, DateTimes.toLocalDate(value.toString()));
                                    } else if (argType == LocalTime.class) {
                                        method.invoke(entity, DateTimes.toLocalTime(value.toString()));
                                    }
                                    break;
                            }
                        } else if (Map.class.isAssignableFrom(argType) || Collection.class.isAssignableFrom(argType) || !argType.getTypeName().startsWith("java.")) {
                            if (valueType.equals("org.postgresql.util.PGobject")) {
                                Class<?> pgObjClass = value.getClass();
                                String pgType = (String) pgObjClass.getDeclaredMethod("getType").invoke(value);
                                String pgValue = (String) pgObjClass.getDeclaredMethod("getValue").invoke(value);
                                if (pgType.equals("json") || pgType.equals("jsonb")) {
                                    method.invoke(entity, json2Obj(pgValue, argType));
                                }
                                // I think this is json string and you want convert to object.
                            } else if (valueType.equals("java.lang.String")) {
                                method.invoke(entity, json2Obj(value.toString(), argType));
                                // PostgreSQL array and exclude blob
                            } else if (valueType.startsWith("[L") || (valueType.endsWith("[]") && !valueType.equals("byte[]"))) {
                                if (List.class.isAssignableFrom(argType)) {
                                    method.invoke(entity, Arrays.asList((Object[]) value));
                                } else if (Set.class.isAssignableFrom(argType)) {
                                    method.invoke(entity, new HashSet<>(Arrays.asList((Object[]) value)));
                                }
                            } else {
                                method.invoke(entity, value);
                            }
                        } else if (argType == String.class) {
                            method.invoke(entity, value.toString());
                        } else {
                            method.invoke(entity, value);
                        }
                    }
                }
            }
            return entity;
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IntrospectionException | IllegalAccessException e) {
            throw new RuntimeException("convert to " + clazz.getTypeName() + "error: ", e);
        }
    }

    public static DataRow2 fromEntity(Object entity) {
        try {
            List<String> names = new ArrayList<>();
            List<String> types = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            Iterator<Method> methods = ReflectUtil.getWRMethods(entity.getClass()).getItem1().iterator();
            while (methods.hasNext()) {
                Method method = methods.next();
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
                        String type = returnType.getTypeName();
                        field = field.substring(0, 1).toLowerCase().concat(field.substring(1));
                        names.add(field);
                        types.add(type);
                        values.add(value);
                    }
                }
            }
            return of(names.toArray(new String[0]), types.toArray(new String[0]), values.toArray());
        } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
            throw new RuntimeException("convert to DataRow error: ", e);
        }
    }

    public static DataRow2 fromMap(Map<?, ?> map) {
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

    public static DataRow2 fromPair(Object... pairs) {
        if (pairs.length == 0 || pairs.length % 2 != 0) {
            throw new IllegalArgumentException("key value are not a pair.");
        }
        String[] names = new String[pairs.length >> 1];
        String[] types = new String[names.length];
        Object[] values = new Object[names.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = pairs[i << 1].toString();
            values[i] = pairs[(i << 1) + 1];
            if (values[i] == null) {
                types[i] = "null";
            } else {
                types[i] = values[i].getClass().getName();
            }
        }
        return of(names, types, values);
    }

    @Override
    public String toString() {
        return "DataRow2{" +
                "grid=" + Arrays.toString(grid) +
                ", columns=" + columns +
                '}';
    }
}
