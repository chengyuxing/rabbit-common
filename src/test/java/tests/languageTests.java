package tests;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.SimpleScriptParser;
import com.github.chengyuxing.common.script.language.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class languageTests {

    static String input;
    static Map<String, Object> context = new HashMap<>();

    @BeforeClass
    public static void init() {
        input = new FileResource("b.txt").readString(StandardCharsets.UTF_8);
        context.put("list", Arrays.asList("a", "b", "a"));
        context.put("age", 12);
        context.put("lx", 'a');
    }

    @Test
    public void testTokens() {
        IdentifierLexer lexer2 = new IdentifierLexer(input);
        lexer2.tokenize().forEach(System.out::println);
    }

    @Test
    public void test1() {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();

        tokens.forEach(System.out::println);

        Parser parser = new Parser(tokens, context);
        String result = parser.parse();
        System.out.println("--------------");
        System.out.println(result);
    }

    @Test
    public void test2() {
        SimpleScriptParser simpleScriptParser = new SimpleScriptParser();
        String res = simpleScriptParser.parse(input, context);
        System.out.println(res);
    }
}
