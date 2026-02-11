package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.lang.Comparators;
import com.github.chengyuxing.common.script.ast.impl.KeyExpressionParser;
import com.github.chengyuxing.common.script.lexer.IdentifierLexer;
import com.github.chengyuxing.common.util.NamingUtils;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.common.util.ValueUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.entity.User;

import java.math.BigDecimal;
import java.util.*;

public class ExpressionTests {
    static String exp = "!(:id >= 0 || :name <> blank) && :age<=21";
    static DataRow args = DataRow.of();

    @BeforeClass
    public static void init() {
        args.put("id", -1);
        args.put("NAME", null);
        args.put("age", 17);
        args.put("name", "cyx");
        args.put("user", DataRow.of("name", "cyx", "addresses", Arrays.asList("a", DataRow.of("age", 88), Arrays.asList(1, 2, 3), "d", "e")));
    }

    @Test
    public void testDecimal() {
        System.out.println(new BigDecimal("+897"));
        System.out.println(Comparators.equals("77", 77));
    }

    @Test
    public void testLe() {
        String key = "user.addresses[1].name 密密麻麻吗";
        if (KeyExpressionParser.EXPRESSION_PATTERN.matcher(key).matches()) {
            System.out.println(key.replaceAll("\\[(\\d+)]", ".$1"));
        }
        KeyExpressionParser parser = new KeyExpressionParser(new IdentifierLexer(key,0).tokenize());
        System.out.println(parser.parse());
//        IdentifierLexer lexer = new IdentifierLexer(key, 0);
//        List<Token> tokens = lexer.tokenize();
//        tokens.forEach(token -> {
//            if (token.getType() == TokenType.IDENTIFIER || token.getType() == TokenType.NUMBER) {
//                System.out.println(token.getValue());
//            }
//        });
    }

    @Test
    public void testPerf() {
        String key = "user.addresses[1].age";
        KeyExpressionParser parser = new KeyExpressionParser(new IdentifierLexer(key,0).tokenize());

        // warm up
        for (int i = 0; i < 1_000_00; i++) {
            Arrays.asList(key.replaceAll("\\[(\\d+)]", ".$1").split("\\."));
            new KeyExpressionParser(new IdentifierLexer(key,0).tokenize()).parse();
        }

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < 10_000_0; i++) {
            Arrays.asList(key.replaceAll("\\[(\\d+)]", ".$1").split("\\."));
        }
        long t2 = System.currentTimeMillis();

        long t3 = System.currentTimeMillis();
        for (int i = 0; i < 10_000_0; i++) {
            new KeyExpressionParser(new IdentifierLexer(key,0).tokenize()).parse();
        }
        long t4 = System.currentTimeMillis();

        System.out.println("replace: " + (t2 - t1));
        System.out.println("lexer  : " + (t4 - t3));
    }

    @Test
    public void testD() {
        User u = new User();
        u.setName("cyx");
        DataRow row = DataRow.of("user", DataRow.of(
                "address", new ArrayList<>(Arrays.asList("a", "b", "c")),
                "info", u
        ));
//        Set<String> sets = row.deepGetAs("user.address", v -> {
//            if (v instanceof List) {
//                return new HashSet<>((Collection<String>) v);
//            }
//            return Collections.emptySet();
//        });
        System.out.println(row.<Object>deepGetAs("user.address[1]"));
//        System.out.println(sets);
//        System.out.println(sets.getClass());
    }

    @Test
    public void testS() {
        System.out.println(StringUtils.isNonNegativeInteger("1111"));
    }

    @Test
    public void testF() {
        int[] ints = new int[]{1, 2, 3};
        List<Integer> list = ValueUtils.adaptValue(List.class, ints);
        System.out.println(list);
        System.out.println(ValueUtils.coalesceNonNull(null, "abc").length());
        System.out.println(StringUtils.countOccurrencesIgnoreCase("user.address.1", "Ss"));
        System.out.println(NamingUtils.kebabToCamel("a-b-user-dd"));
        System.out.println("mmm".indexOf(null));
    }
}
