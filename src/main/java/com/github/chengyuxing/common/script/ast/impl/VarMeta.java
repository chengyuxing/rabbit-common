package com.github.chengyuxing.common.script.ast.impl;

import java.util.Objects;

public class VarMeta {
    private final String name;
    private final Object value;
    private final long id;

    public VarMeta(String name, Object value, long id) {
        this.name = name;
        this.value = value;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public long getId() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof VarMeta)) return false;

        VarMeta varMeta = (VarMeta) o;
        return getId() == varMeta.getId() && getName().equals(varMeta.getName()) && Objects.equals(getValue(), varMeta.getValue());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + Objects.hashCode(getValue());
        result = 31 * result + Long.hashCode(getId());
        return result;
    }

    @Override
    public String toString() {
        if (value == null) return "";
        return value.toString();
    }
}
