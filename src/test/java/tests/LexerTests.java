package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.TokenType;
import com.github.chengyuxing.common.script.lexer.IdentifierLexer;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import com.github.chengyuxing.common.script.parser.RabbitScriptParser;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    @Test
    public void testPipes() {
        RabbitScriptParser parser = new RabbitScriptParser(pipes);
        parser.verify();
        String res = parser.parse(
                DataRow.of("name", "cyx",
                        "address", "kunming",
                        "id", 1,
                        "list", DataRow.of("id", 1, "age", 22),
                        "users", "a,b,c,d",
                        "home", null)
        );
        System.out.println(res);
    }

    @Test
    public void testCheck() {
        RabbitScriptParser parser = new RabbitScriptParser(check);
        parser.verify();
        System.out.println(parser.parse(DataRow.of("id", 90)));
    }

    @Test
    public void testGuard() {
        System.out.println(new Token(TokenType.IF, "#if", 0, 0));
        RabbitScriptParser parser = new RabbitScriptParser(guard);
        parser.verify();
        String res = parser.parse(DataRow.of("id", 10));
        System.out.println(res);
    }

    @Test
    public void testLexer() {
        RabbitScriptLexer lexer = new RabbitScriptLexer(query) {
            @Override
            protected String trimExpressionLine(String line) {
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
        RabbitScriptParser parser = new RabbitScriptParser(query) {
            @Override
            protected String trimExpressionLine(String line) {
                String tl = line.trim();
                if (tl.startsWith("--")) {
                    return tl.substring(2).trim();
                }
                return line;
            }
        };
        parser.verify();
        System.out.println(parser.parse(DataRow.of(
                "username", "cyx",
                "password", "123456"
        )));
    }

    @Test
    public void test6() {
        RabbitScriptParser parser = new RabbitScriptParser(choose);
        parser.verify();
        String res = parser.parse(DataRow.of(
                "id", "B"));
        System.out.println(res);
    }

    @Test
    public void test5() {
        RabbitScriptParser parser = new RabbitScriptParser(If);
        parser.verify();
        String res = parser.parse(DataRow.of(
                "jssj", "nubll",
                "kssj", "2022-12-12",
                "name", "cyx",
                "id", "2"));
        System.out.println(res);
    }

    @Test
    public void test1() {
        RabbitScriptParser lexer = new RabbitScriptParser(If);
        lexer.verify();
    }

    @Test
    public void test2() {
        RabbitScriptParser parser = new RabbitScriptParser(Switch);
        String res = parser.parse(DataRow.of("name", "ak"));
        System.out.println(res);
    }

    @Test
    public void test3() {
        RabbitScriptParser parser = new RabbitScriptParser(for1);
        parser.verify();
        String res = parser.parse(DataRow.of("names", Arrays.asList('a', 'b', 'c')));
        System.out.println(res);
    }

    @Test
    public void testLexer1() {
        IdentifierLexer lexer = new IdentifierLexer("123abc", 0);
        lexer.tokenize().forEach(System.out::println);
    }

}
