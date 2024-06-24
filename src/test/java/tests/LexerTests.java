package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.lexer.FlowControlLexer;
import com.github.chengyuxing.common.script.parser.FlowControlParser;
import org.junit.Test;

import java.util.Arrays;

public class LexerTests {

    static String s1 = "#for item,idx of :names delimiter ' or' open '(' close ')'\n" +
            "${item}:${idx}\n" +
            "#done";
    static String s2 = "#for item, idx of :user.n_ames | 3.14 | upp5er delimiter ' or' open '(' close ')'\n" +
            "#done";
    static String s3 = "#switch :name | upper | lower\n" +
            "#case 'a' ,'b','c'\n" +
            "aaaaaaaaa\n" +
            "#break\n" +
            "#case 3.14\n" +
            "ooooooooo\n" +
            "#break\n" +
            "#default\n" +
            "rrrrrrrrr\n" +
            "#break\n" +
            "#end";

    @Test
    public void test1() {
        FlowControlLexer lexer = new FlowControlLexer(s3);
        lexer.tokenize().forEach(System.out::println);
    }

    @Test
    public void test2() {
        FlowControlParser parser = new FlowControlParser();
        String res = parser.parse(s3, DataRow.of("name", 3.14));
        System.out.println(res);
    }

    @Test
    public void test3() {
        FlowControlParser parser = new FlowControlParser();
        String res = parser.parse(s1, DataRow.of("names", Arrays.asList('a', 'b', 'c')));
        System.out.println(res);
    }
}
