package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.RabbitScriptEngine;
import com.github.chengyuxing.common.script.ast.ScriptAst;
import com.github.chengyuxing.common.script.ast.ScriptEngine;
import com.github.chengyuxing.common.script.ast.impl.EvalContext;
import com.github.chengyuxing.common.script.ast.impl.EvalResult;
import com.github.chengyuxing.common.script.lexer.IdentifierLexer;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.script.pipe.IPipe;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class LexerTests {

    static String for1 = new FileResource("flow-control/for1.txt").readString(StandardCharsets.UTF_8);
    static String for2 = new FileResource("flow-control/for2.txt").readString(StandardCharsets.UTF_8);
    static String Switch = new FileResource("flow-control/switch.txt").readString(StandardCharsets.UTF_8);
    static String If = new FileResource("flow-control/if.txt").readString(StandardCharsets.UTF_8);
    static String choose = new FileResource("flow-control/choose.txt").readString(StandardCharsets.UTF_8);
    static String guard = new FileResource("flow-control/guard.txt").readString(StandardCharsets.UTF_8);
    static String check = new FileResource("flow-control/check.txt").readString(StandardCharsets.UTF_8);
    static String pipes = new FileResource("flow-control/pipes.txt").readString(StandardCharsets.UTF_8);
    static String query = new FileResource("query.txt").readString(StandardCharsets.UTF_8);
    static String var = new FileResource("flow-control/var.txt").readString(StandardCharsets.UTF_8);

    @Test
    public void testVar() {
        RabbitScriptEngine engine = new RabbitScriptEngine();
        ScriptAst ast = engine.compile(var);
        EvalResult res = engine.execute(ast, new EvalContext(DataRow.of(
                "user", DataRow.of("addresses", Arrays.asList("a", DataRow.of("name", "kunming"), "c")))));
        System.out.println(res.getContent());
        System.out.println(res.getUsedVars());
    }

    @Test
    public void testPipes() {
        RabbitScriptEngine engine = new RabbitScriptEngine();

        ScriptAst ast = engine.compile(pipes);

        EvalContext context = new EvalContext(DataRow.of("name", "cyx",
                "address", "kunming",
                "id", 5,
                "list", DataRow.of("id", 1, "age", 22),
                "users", "a,b,c,d",
                "home", null)) {
            @Override
            protected @NotNull Map<String, IPipe<?>> getPipes() {
                return Collections.singletonMap("x", new X());
            }
        };

        EvalResult res = engine.execute(ast, context);
        System.out.println(res);
        System.out.println(res.getContent());
        System.out.println(res.getUsedVars());
    }

    @Test
    public void testCheck() {
        RabbitScriptEngine engine = new RabbitScriptEngine();
        ScriptAst ast = engine.compile(check);
        EvalResult result = engine.execute(ast, new EvalContext(DataRow.of("id", 90)));
        System.out.println(result);
    }

    @Test
    public void testGuard() {
        RabbitScriptEngine parser = new RabbitScriptEngine();
        EvalResult res = parser.run(guard, DataRow.of("id", 90));
        System.out.println(res);
    }

    @Test
    public void testLexer() {
        RabbitScriptLexer lexer = new RabbitScriptLexer(query) {
            @Override
            protected String normalizeDirectiveLine(String line) {
                if (line.trim().startsWith("--")) {
                    return line.trim().substring(2);
                }
                return line;
            }
        };
        lexer.tokenize().forEach(System.out::println);
    }

    @Test
    public void test7() {
        RabbitScriptEngine engine = new RabbitScriptEngine(line -> {
            String tl = line.trim();
            if (tl.startsWith("--")) {
                return tl.substring(2).trim();
            }
            return line;
        });
        ScriptAst ast = engine.compile(query);
        EvalResult result = engine.execute(ast, new EvalContext(DataRow.of(
                "username", "cyx",
                "password", "123456"
        )));
        System.out.println(result);
    }

    @Test
    public void test6() {
        RabbitScriptEngine engine = new RabbitScriptEngine();
        EvalResult res = engine.run(choose, DataRow.of(
                "id", "B"));
        System.out.println(res);
    }

    @Test
    public void test5() {
        ScriptEngine e = new RabbitScriptEngine();
        EvalResult res = e.run(If, DataRow.of(
                "jssj", "nubll",
                "kssj", "2022-12-12",
                "name", "cyx",
                "id", "2"));
        System.out.println(res);
    }

    @Test
    public void test2() {
        ScriptEngine engine = new RabbitScriptEngine();
        EvalResult res = engine.run(Switch, DataRow.of("name", "ak"));
        System.out.println(res);
    }

    @Test
    public void test3() {
        RabbitScriptEngine engine = new RabbitScriptEngine();
        EvalResult res = engine.run(for1, DataRow.of("names", Arrays.asList('a', 'b', 'c')));
        System.out.println(res);
    }

    @Test
    public void testLexer1() {
        IdentifierLexer lexer = new IdentifierLexer("#if :id.name = 'aaa'", 0);
        lexer.tokenize().forEach(System.out::println);
    }

}
