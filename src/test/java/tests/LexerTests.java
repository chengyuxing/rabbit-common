package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.script.parser.FlowControlParser;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class LexerTests {

    static String for1 = new FileResource("flow-control/for1.txt").readString(StandardCharsets.UTF_8);
    static String for2 = new FileResource("flow-control/for2.txt").readString(StandardCharsets.UTF_8);
    static String Switch = new FileResource("flow-control/switch.txt").readString(StandardCharsets.UTF_8);
    static String If = new FileResource("flow-control/if.txt").readString(StandardCharsets.UTF_8);
    static String choose = new FileResource("flow-control/choose.txt").readString(StandardCharsets.UTF_8);

    @Test
    public void test6() {
        FlowControlParser parser = new FlowControlParser();
        String res = parser.parse(choose, DataRow.of(
                "id", "C"));
        System.out.println(res);
    }

    @Test
    public void test5() {
        FlowControlParser parser = new FlowControlParser();
        String res = parser.parse(If, DataRow.of(
                "jssj", "nubll",
                "kssj", "2022-12-12",
                "name", "cyx",
                "id", "1"));
        System.out.println(res);
    }

    @Test
    public void test1() {
        FlowControlLexer lexer = new FlowControlLexer(If);
        lexer.tokenize().forEach(System.out::println);
    }

    @Test
    public void test2() {
        FlowControlParser parser = new FlowControlParser();
        String res = parser.parse(Switch, DataRow.of("name", "b"));
        System.out.println(res);
    }

    @Test
    public void test3() {
        FlowControlParser parser = new FlowControlParser();
        String res = parser.parse(for1, DataRow.of("names", Arrays.asList('a', 'b', 'c')));
        System.out.println(res);
    }

    @Test
    public void test10() {
        User user = new User();
        user.setAddress("kunming");

        User userR = user;

        User user1 = new User();
        user.setAddress("beijing");

        user = user1;

        System.out.println(userR);
    }
}
