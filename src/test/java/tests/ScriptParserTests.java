package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.Token;
import com.github.chengyuxing.common.script.lexer.IdentifierLexer;
import com.github.chengyuxing.common.script.parser.RabbitScriptParser;
import com.github.chengyuxing.common.KeyValue;
import com.github.chengyuxing.common.script.pipe.builtin.Kv;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.*;

public class ScriptParserTests {

    @Test
    public void testSS() {
        IdentifierLexer lexer = new IdentifierLexer(":{id|date('yyyy-mm-dd',12)|upper|trim}", 0);
        List<Token> tokens = lexer.tokenize();
        tokens.forEach(System.out::println);
    }

    @Test
    public void testSqlParser() {
//        SimpleParser parser = new SimpleParser() {
//            public static final String FOR_VARS_KEY = "_for";
//            public static final String VAR_PREFIX = FOR_VARS_KEY + ".";
//
//            @Override
//            protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, String body, Map<String, Object> args) {
//                String formatted = StringUtil.FMT.format(String.join(NEW_LINE, body), args);
//                if (Objects.nonNull(varName)) {
//                    String varParam = VAR_PREFIX + forVarKey(varName, forIndex, varIndex);
//                    formatted = formatted.replace(VAR_PREFIX + varName, varParam);
//                }
//                if (Objects.nonNull(idxName)) {
//                    String idxParam = VAR_PREFIX + forVarKey(idxName, forIndex, varIndex);
//                    formatted = formatted.replace(VAR_PREFIX + idxName, idxParam);
//                }
//                return formatted;
//            }
//        };
//        List<Map<String, Object>> data = new ArrayList<>();
//        for (int i = 0; i < 200; i++) {
//            String sql = parser.parse("select * from test.user\n where id = 1\n" +
//                    " #for id of :ids delimiter ', ' open ' or id in (' close ')'\n" +
//                    "    #for add of :address\n" +
//                    "       :_for.add\n" +
//                    "       #if :id >= 2\n" +
//                    "       :_for.id\n" +
//                    "       #fi\n" +
//                    "    #done\n" +
//                    " #done", DataRow.of("ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 1 + i, 11, 23, 45, 55, 67),
//                    "address", Arrays.asList("a", "b", "c")));
//            data.add(parser.getForGeneratedVars());
//            if (i == 1) {
//                System.out.println(sql);
//                System.out.println(parser.getForGeneratedVars().size());
//            }
//        }
//        System.out.println(data.size());
    }

    @Test
    public void testField() throws NoSuchFieldException {
        Field field = User.class.getDeclaredField("name");
        System.out.println(field.getType());
    }

    @Test
    public void testSqlLexer() {
        String sql = "select * from test.user\n    where id = 1\n" +
                " #for id of :ids delimiter ', ' open ' or id in (' close ')'\n" +
                "    #for add of :address\n" +
                "       :_for.add\n" +
                "       #if :id == 2\n" +
                "       :_for.id\n" +
                "       #fi\n" +
                "    #done\n" +
                " #done";
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            RabbitScriptParser parser = new RabbitScriptParser(sql) {
                public static final String FOR_VARS_KEY = "_for";
                public static final String VAR_PREFIX = FOR_VARS_KEY + ".";

                @Override
                protected String forLoopBodyFormatter(int forIndex, int itemIndex, String varName, String idxName, String body, Map<String, Object> args) {
                    String formatted = StringUtil.FMT.format(body, args);
                    if (!varName.isEmpty()) {
                        String varParam = VAR_PREFIX + forVarGeneratedKey(varName, forIndex, itemIndex);
                        formatted = formatted.replace(VAR_PREFIX + varName, varParam);
                    }
                    if (!idxName.isEmpty()) {
                        String idxParam = VAR_PREFIX + forVarGeneratedKey(idxName, forIndex, itemIndex);
                        formatted = formatted.replace(VAR_PREFIX + idxName, idxParam);
                    }
                    return formatted;
                }
            };
            parser.verify();
            String res = parser.parse(DataRow.of("ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 1 + i, 11, 23, 45, 55, 67),
                    "address", Arrays.asList("a", "b", "c")));
            data.add(parser.getForGeneratedVars());
            if (i == 1) {
                System.out.println(res);
                System.out.println(parser.getForGeneratedVars().size());
                System.out.println(parser.getForGeneratedVars());
            }
        }
        System.out.println(data.size());
    }

    @Test
    public void test1() {
        Kv kv = new Kv();
        User user = new User();
        user.setName("cyx");
        user.setAge(27);
        user.setAddress("kunming");
        List<KeyValue> keyValues = kv.transform(user);
        System.out.println(keyValues);
    }
}
