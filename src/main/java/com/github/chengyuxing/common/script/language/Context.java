package com.github.chengyuxing.common.script.language;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private final Map<String, Object> variables = new HashMap<>();

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Map<String, Object> getVariables() {
        return variables;
    }
}
