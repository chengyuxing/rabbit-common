package com.github.chengyuxing.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PropertyMeta {
    private final String name;
    private Field field;
    private Method getter;
    private Method setter;

    public PropertyMeta(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }
}