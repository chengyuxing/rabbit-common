package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.*;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

public class LanguageTests {

    static String input;
    static Map<String, Object> context = new HashMap<>();

    @BeforeClass
    public static void init() {
        input = new FileResource("b.txt").readString(StandardCharsets.UTF_8);
        context.put("user", DataRow.of("names", Arrays.asList("a", "b", "A", "e", "f")));
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
        FlowControlLexer lexer = new FlowControlLexer(input);
        List<Token> tokens = lexer.tokenize();

        FlowControlParser parser = new FlowControlParser(tokens, context) {
            public static final String FOR_VARS_KEY = "_for";
            public static final String VAR_PREFIX = FOR_VARS_KEY + ".";

            @Override
            protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String body, Map<String, Object> args) {
                String formatted = StringUtil.FMT.format(String.join(NEW_LINE, body), args);
                if (Objects.nonNull(varName)) {
                    String varParam = VAR_PREFIX + forVarKey(varName, forIndex, varIndex);
                    formatted = formatted.replace(VAR_PREFIX + varName, varParam);
                }
                return formatted;
            }
        };
        String result = parser.parse();
        System.out.println("--------------");
        System.out.println(result);
        System.out.println(parser.getForContextVars());
    }

    @Test
    public void test2() {
        SimpleScriptParser simpleScriptParser = new SimpleScriptParser();
        String res = simpleScriptParser.parse(input, context);
        System.out.println(res);
    }
}
