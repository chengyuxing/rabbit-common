package com.github.chengyuxing.common.script.parser;

import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.expression.impl.FastExpression;
import com.github.chengyuxing.common.utils.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.script.expression.Patterns.*;
import static com.github.chengyuxing.common.script.expression.Patterns.STRING_PATTERN;

/**
 * <h2>Abstract Flow-Control parser</h2>
 * <p>if statement:</p>
 * <blockquote>
 * <pre>
 * #if {@linkplain FastExpression expression1}
 *      #if {@linkplain FastExpression expression2}
 *      ...
 *      #fi
 *      #if {@linkplain FastExpression expression3}
 *      ...
 *      #else
 *      ...
 *      #fi
 * #fi
 * </pre>
 * </blockquote>
 * <p>choose statement:</p>
 * <blockquote>
 * <pre>
 * #choose
 *      #when {@linkplain FastExpression expression1}
 *      ...
 *      #break
 *      #when {@linkplain FastExpression expression2}
 *      ...
 *      #break
 *      ...
 *      #default
 *      ...
 *      #break
 * #end
 * </pre>
 * </blockquote>
 * <p>switch statement</p>
 * <blockquote>
 * <pre>
 * #switch :key [| {@linkplain IPipe pipe1} | {@linkplain IPipe pipeN} | ...]
 *      #case var1
 *      ...
 *      #break
 *      #case var2
 *      ...
 *      #break
 *      ...
 *      #default
 *      ...
 *      #break
 * #end
 * </pre>
 * </blockquote>
 * <p>for statement</p>
 * <blockquote>
 * <pre>
 * #for item[,idx] of :list [| {@linkplain IPipe pipe1} | pipeN | ... ] [delimiter ','] [open ''] [close '']
 *     ...
 * #done
 * </pre>
 * </blockquote>
 *
 * @see FastExpression
 */
public abstract class AbstractParser {
    public static final Pattern FOR_PATTERN = Pattern.compile("(?<item>\\w+)(\\s*,\\s*(?<index>\\w+))?\\s+of\\s+:(?<list>" + VAR_KEY_PATTERN + ")(?<pipes>" + PIPES_PATTERN + ")?(\\s+delimiter\\s+(?<delimiter>" + STRING_PATTERN + "))?(\\s+open\\s+(?<open>" + STRING_PATTERN + "))?(\\s+close\\s+(?<close>" + STRING_PATTERN + "))?");

    protected Map<String, Object> forContextVars = new HashMap<>();

    /**
     * Parse content with scripts.
     *
     * @return parsed content
     * @see IExpression
     */
    public abstract String parse(String input, Map<String, Object> context);

    /**
     * Get {@code #for} context variable map which saved by expression calc.<br>
     * Format: {@code (varName_forAutoIdx_varAutoIdx: var)}, e.g.
     * <blockquote>
     * <pre>
     * list: ["a", "b", "c"]; forIdx: 0
     * </pre>
     * <pre>
     * #for item of :list
     *      ...
     * #done
     * </pre>
     * <pre>
     * vars: {item_0_0: "a", item_0_1: "b", item_0_2: "c"}
     * </pre>
     * </blockquote>
     *
     * @return {@code #for} context variable map
     * @see #forVarKey(String, int, int)
     */
    public Map<String, Object> getForContextVars() {
        return Collections.unmodifiableMap(forContextVars);
    }

    /**
     * Trim each line for search prefix {@code #} to detect expression.
     *
     * @param line current line
     * @return expression or normal line
     */
    protected String trimExpression(String line) {
        String tl = line.trim();
        if (tl.startsWith("#")) {
            return tl;
        }
        return line;
    }

    /**
     * Configure expression parser implementation.
     *
     * @param expression expression
     * @return expression parser implementation
     */
    protected IExpression expression(String expression) {
        return FastExpression.of(expression);
    }

    /**
     * Build {@code #for} var key.
     *
     * @param name   for context var name
     * @param forIdx for auto index
     * @param varIdx var auto index
     * @return unique for var key
     */
    protected String forVarKey(String name, int forIdx, int varIdx) {
        return name + "_" + forIdx + "_" + varIdx;
    }

    /**
     * <code>#for</code> loop body content formatter, format custom template variable and args resolve, e.g.
     * <p>args:</p>
     * <blockquote>
     * <pre>
     * {
     *   users: [
     *     {name: 'cyx', name: 'json'}
     *   ]
     * }
     * </pre>
     * </blockquote>
     * <p>for expression:</p>
     * <blockquote>
     * <pre>
     * #for user,idx of :users delimiter ' and '
     *    '${user.name}'
     * #done
     * </pre>
     * </blockquote>
     * <p>result:</p>
     * <blockquote>
     * <pre>'cyx' and 'json'</pre>
     * </blockquote>
     *
     * @param forIndex each for loop auto index
     * @param varIndex for var auto index
     * @param varName  for context var name,  e.g. {@code <user>}
     * @param idxName  for context index name,  e.g. {@code <idx>}
     * @param body     content in for loop
     * @param context  each for loop context args (index and value) which created by for expression
     * @return formatted content
     * @see #getForContextVars()
     */
    protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, String body, Map<String, Object> context) {
        return StringUtil.FMT.format(body, context);
    }
}
