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
    static DataRow d = DataRow.of(
            "c", "blank",
            "c1", "blank",
            "c2", "blank",
            "data", DataRow.of(
                    "name", "chengyuxing",
                    "age", 30,
                    "address", "昆明市"
            ),
            "ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8),
            "list", Arrays.asList(
                    "A",
                    "B",
                    DataRow.of(
                            "nums",
                            Arrays.asList("1", "2", "3")),
                    "D",
                    "E")
    );

    @BeforeClass
    public static void init() {
        input = new FileResource("b.txt").readString(StandardCharsets.UTF_8);
        context.put("user", DataRow.of("names", Arrays.asList("cyx", "mike", "JACK.SON", "John", "Lisa")));
        context.put("age", 11);
        context.put("lx", 'a');
    }

    @Test
    public void testTokens() {
        IdentifierLexer lexer2 = new IdentifierLexer(input);
        lexer2.tokenize().forEach(System.out::println);
    }

    @Test
    public void test1() {
        FlowControlParser parser = new FlowControlParser() {
            public static final String FOR_VARS_KEY = "_for";
            public static final String VAR_PREFIX = FOR_VARS_KEY + ".";

            @Override
            protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, String body, Map<String, Object> args) {
                String formatted = StringUtil.FMT.format(body, args);
                if (Objects.nonNull(varName)) {
                    String varParam = VAR_PREFIX + forVarKey(varName, forIndex, varIndex);
                    formatted = formatted.replace(VAR_PREFIX + varName, varParam);
                }
                if (Objects.nonNull(idxName)) {
                    String idxParam = VAR_PREFIX + forVarKey(idxName, forIndex, varIndex);
                    formatted = formatted.replace(VAR_PREFIX + idxName, idxParam);
                }
                return formatted;
            }
        };
        String result = parser.parse(input, context);
        System.out.println("--------------");
        System.out.println(result);
        System.out.println(parser.getForContextVars());
    }

    @Test
    public void test2() {
        SimpleScriptParser simpleScriptParser = new SimpleScriptParser();
        String res = simpleScriptParser.parse(input, context);
        System.out.println(res);
        System.out.println(simpleScriptParser.getForContextVars());
    }
}
