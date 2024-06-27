package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.parser.FlowControlParser;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LexerTests {

    static String for1 = new FileResource("flow-control/for1.txt").readString(StandardCharsets.UTF_8);
    static String for2 = new FileResource("flow-control/for2.txt").readString(StandardCharsets.UTF_8);
    static String Switch = new FileResource("flow-control/switch.txt").readString(StandardCharsets.UTF_8);
    static String If = new FileResource("flow-control/if.txt").readString(StandardCharsets.UTF_8);
    static String choose = new FileResource("flow-control/choose.txt").readString(StandardCharsets.UTF_8);
    static String query = new FileResource("query.txt").readString(StandardCharsets.UTF_8);

    @Test
    public void test7() {
        FlowControlParser parser = new FlowControlParser(query) {
            @Override
            protected String trimExpression(String line) {
                String tl = line.trim();
                if (tl.startsWith("--")) {
                    String kl = tl.substring(2).trim();
                    if (kl.startsWith("#")) {
                        return kl;
                    }
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
        FlowControlParser parser = new FlowControlParser(choose);
        parser.verify();
        String res = parser.parse(DataRow.of(
                "id", "B"));
        System.out.println(res);
    }

    @Test
    public void test5() {
        FlowControlParser parser = new FlowControlParser(If);
        parser.verify();
        String res = parser.parse(DataRow.of(
                "jssj", "nubll",
                "kssj", "2022-12-12",
                "name", "cyx",
                "id", "1"));
        System.out.println(res);
    }

    @Test
    public void test1() {
        FlowControlParser lexer = new FlowControlParser(If);
        lexer.verify();
    }

    @Test
    public void test2() {
        FlowControlParser parser = new FlowControlParser(Switch);
        String res = parser.parse(DataRow.of("name", "ak"));
        System.out.println(res);
    }

    @Test
    public void test3() {
        FlowControlParser parser = new FlowControlParser(for1);

        String res = parser.parse(DataRow.of("names", Arrays.asList('a', 'b', 'c')));
        System.out.println(res);
    }
}
