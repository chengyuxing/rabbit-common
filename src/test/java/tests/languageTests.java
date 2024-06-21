package tests;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.SimpleScriptParser;
import com.github.chengyuxing.common.script.language.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class languageTests {

    static String input;
    static Context context = new Context();

    @BeforeClass
    public static void init() {
        input = new FileResource("b.txt").readString(StandardCharsets.UTF_8);
        context.setVariable("list", Arrays.asList("a", "b", "a"));
        context.setVariable("age", 12);
        context.setVariable("lx", 'a');
    }

    @Test
    public void testTokens() {
        Lexer lexer2 = new Lexer(input);
        lexer2.tokenize().forEach(System.out::println);
    }

    @Test
    public void test1() {
        IdentifierLexer lexer = new IdentifierLexer(input);
        List<Token> tokens = lexer.tokenize();

        tokens.forEach(System.out::println);

        Parser parser = new Parser(tokens, context.getVariables());
        String result = parser.parse();
        System.out.println("--------------");
        System.out.println(result);
    }

    @Test
    public void test2() {
        SimpleScriptParser simpleScriptParser = new SimpleScriptParser();
        String res = simpleScriptParser.parse(input, context.getVariables());
        System.out.println(res);
    }
}
