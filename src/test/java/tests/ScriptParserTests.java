package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.FlowControlLexer;
import com.github.chengyuxing.common.script.FlowControlParser;
import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.SimpleScriptParser;
import com.github.chengyuxing.common.KeyValue;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.util.*;

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

public class ScriptParserTests {
    @Test
    public void testSqlParser() {
        SimpleScriptParser parser = new SimpleScriptParser();
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            String sql = parser.parse("select * from test.user\n where id = 1\n" +
                    " #for id of :ids delimiter ', ' open ' or id in (' close ')'\n" +
                    "    #for add of :address\n" +
                    "       :_for.add\n" +
                    "       #if :id >= 2\n" +
                    "       :_for.id\n" +
                    "       #fi\n" +
                    "    #done\n" +
                    " #done", DataRow.of("ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 1 + i, 11, 23, 45, 55, 67),
                    "address", Arrays.asList("a", "b", "c")));
            data.add(parser.getForContextVars());
            if (i == 9999) {
                System.out.println(sql);
                System.out.println(parser.getForContextVars());
            }
        }
        System.out.println(data.size());
    }

    @Test
    public void testSqlLexer() {
        String sql = "select * from test.user\n where id = 1\n" +
                " #for id of :ids delimiter ', ' open ' or id in (' close ')'\n" +
                "    #for add of :address\n" +
                "       :_for.add\n" +
                "       #if :id >= 2\n" +
                "       :_for.id\n" +
                "       #fi\n" +
                "    #done\n" +
                " #done";
        FlowControlLexer lexer = new FlowControlLexer(sql);
        List<Token> tokens = lexer.tokenize();
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            FlowControlParser parser = new FlowControlParser(tokens, DataRow.of("ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 1 + i, 11, 23, 45, 55, 67),
                    "address", Arrays.asList("a", "b", "c"))) {
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
            String res = parser.parse();
            data.add(parser.getForContextVars());
            if (i == 1) {
                System.out.println(res);
                System.out.println(parser.getForContextVars());
            }
        }
        System.out.println(data.size());
    }

    @Test
    public void test1() {
        IPipe.Kv kv = new IPipe.Kv();
        User user = new User();
        user.setName("cyx");
        user.setAge(27);
        user.setAddress("kunming");
        List<KeyValue> keyValues = kv.transform(user);
        System.out.println(keyValues);
    }
}
