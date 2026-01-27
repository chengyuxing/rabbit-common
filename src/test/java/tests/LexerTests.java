package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.lexer.IdentifierLexer;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.script.RabbitScriptEngine;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

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
        RabbitScriptEngine engine = new RabbitScriptEngine(var);
        String res = engine.evaluate(DataRow.of(
                "user", DataRow.of("addresses", Arrays.asList("a", DataRow.of("name", "kunming"), "c"))));
        System.out.println(res);
    }

    @Test
    public void testPipes() {
        RabbitScriptEngine parser = new RabbitScriptEngine(pipes);
        parser.setPipes(Collections.singletonMap("x", new X()));
        parser.verify();
        String res = parser.evaluate(
                DataRow.of("name", "cyx",
                        "address", "kunming",
                        "id", 5,
                        "list", DataRow.of("id", 1, "age", 22),
                        "users", "a,b,c,d",
                        "home", null)
        );
        System.out.println(res);
        System.out.println(parser.getForGeneratedVars());
        System.out.println(parser.getDefinedVars());
    }

    @Test
    public void testCheck() {
        RabbitScriptEngine parser = new RabbitScriptEngine(check);
        parser.verify();
        System.out.println(parser.evaluate(DataRow.of("id", 90)));
    }

    @Test
    public void testGuard() {
        RabbitScriptEngine parser = new RabbitScriptEngine(guard);
        parser.verify();
        String res = parser.evaluate(DataRow.of("id", 90));
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
        RabbitScriptEngine parser = new RabbitScriptEngine(query) {
            @Override
            protected String normalizeDirectiveLine(String line) {
                String tl = line.trim();
                if (tl.startsWith("--")) {
                    return tl.substring(2).trim();
                }
                return line;
            }
        };
        parser.verify();
        System.out.println(parser.evaluate(DataRow.of(
                "username", "cyx",
                "password", "123456"
        )));
    }

    @Test
    public void test6() {
        RabbitScriptEngine parser = new RabbitScriptEngine(choose);
        parser.verify();
        String res = parser.evaluate(DataRow.of(
                "id", "B"));
        System.out.println(res);
    }

    @Test
    public void test5() {
        RabbitScriptEngine parser = new RabbitScriptEngine(If);
        parser.verify();
        String res = parser.evaluate(DataRow.of(
                "jssj", "nubll",
                "kssj", "2022-12-12",
                "name", "cyx",
                "id", "2"));
        System.out.println(res);
    }

    @Test
    public void test1() {
        RabbitScriptEngine lexer = new RabbitScriptEngine(If);
        lexer.verify();
    }

    @Test
    public void test2() {
        RabbitScriptEngine parser = new RabbitScriptEngine(Switch);
        String res = parser.evaluate(DataRow.of("name", "ak"));
        System.out.println(res);
    }

    @Test
    public void test3() {
        RabbitScriptEngine parser = new RabbitScriptEngine(for1);
        parser.verify();
        String res = parser.evaluate(DataRow.of("names", Arrays.asList('a', 'b', 'c')));
        System.out.println(res);
    }

    @Test
    public void testLexer1() {
        IdentifierLexer lexer = new IdentifierLexer("123abc", 0);
        lexer.tokenize().forEach(System.out::println);
    }

}
