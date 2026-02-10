package com.github.chengyuxing.common.script.ast;

import com.github.chengyuxing.common.script.ast.impl.VarMeta;
import com.github.chengyuxing.common.tuple.Pair;

import java.util.Map;

public interface PlainTextFormatter {
    Pair<String, Map<String, Object>> format(String text, Map<String, Object> inputs, Map<String, VarMeta> scope);
}
