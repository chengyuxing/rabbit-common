package com.github.chengyuxing.common.script;

import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.ScriptAst;
import com.github.chengyuxing.common.script.ast.ScriptEngine;
import com.github.chengyuxing.common.script.ast.impl.*;
import com.github.chengyuxing.common.script.lang.Comparators;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.script.pipe.IPipe;

import java.util.List;
import java.util.function.Function;

/**
 * <h2>Rabbit script engine.</h2>
 * <p>check statement:</p>
 * <blockquote>
 * <pre>
 * #check expression throw 'message'
 * </pre>
 * </blockquote>
 * <p>var statement:</p>
 * <blockquote>
 * <pre>
 * #var myVal = :key [| {@linkplain IPipe pipe1} | {@linkplain IPipe pipeN} | ...]
 * </pre>
 * </blockquote>
 * <p>if statement:</p>
 * <blockquote>
 * <pre>
 * #if <i>expression1</i>
 *      #if <i>expression2</i>
 *      ...
 *      #fi
 *      #if <i>expression3</i>
 *      ...
 *      #else
 *      ...
 *      #fi
 * #fi
 * </pre>
 * </blockquote>
 * <p>guard statement:</p>
 * <blockquote>
 * <pre>
 * #guard <i>expression</i>
 *     ...
 * #throw 'message'
 * </pre>
 * </blockquote>
 * <p>choose statement:</p>
 * <blockquote>
 * <pre>
 * #choose
 *      #when <i>expression1</i>
 *      ...
 *      #break
 *      #when <i>expression2</i>
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
 *      #case var1[, var2][, varN],...
 *      ...
 *      #break
 *      #case var3
 *      ...
 *      #break
 *      ...
 *      #default
 *      ...
 *      #break
 * #end
 * </pre>
 * </blockquote>
 * <p>for statement (provides the {@link com.github.chengyuxing.common.script.lang.ForContextProperty context property} by use syntax {@code property as name})</p>
 * <blockquote>
 * <pre>
 * #for item of :list [| {@linkplain IPipe pipe1} | pipeN | ... ] [;index as i] [;last as isLast] ...
 *     ...
 * #done
 * </pre>
 * </blockquote>
 * <p>Boolean condition expression.</p>
 * <p>Support logic operator: {@code &&, ||, !}, e.g.</p>
 * <blockquote><pre>!(:id &gt;= 0 || :name | {@link com.github.chengyuxing.common.script.pipe.builtin.Nvl nvl('guest')} | {@link com.github.chengyuxing.common.script.pipe.builtin.Length length} &lt;= 3) &amp;&amp; :isHuman
 * </pre></blockquote>
 * Built-in {@link IPipe pipes}：{@link com.github.chengyuxing.common.script.pipe.BuiltinPipes}
 *
 * @see Comparators
 */
public final class RabbitScriptEngine implements ScriptEngine {
    private final Function<String, String> directiveNormalizer;

    public RabbitScriptEngine() {
        this.directiveNormalizer = Function.identity();
    }

    /**
     * Construct a new RabbitScriptEngine instance with the directiveNormalizer.
     *
     * @param directiveNormalizer the function to trim the line to directive
     */
    public RabbitScriptEngine(Function<String, String> directiveNormalizer) {
        this.directiveNormalizer = directiveNormalizer;
    }

    @Override
    public ScriptAst compile(String script) {
        RabbitScriptLexer lexer = new RabbitScriptLexer(script) {
            @Override
            protected String normalizeDirectiveLine(String line) {
                return directiveNormalizer.apply(line);
            }
        };
        RabbitScriptParser parser = new RabbitScriptParser(lexer.tokenize());
        List<IElement> elements = parser.parse();
        return new ScriptAst(elements);
    }

    @Override
    public EvalResult execute(ScriptAst ast, EvalContext context) {
        RabbitScriptEvaluator evaluator = new RabbitScriptEvaluator(context);
        return evaluator.execute(ast);
    }
}
