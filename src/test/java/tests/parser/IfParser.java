package tests.parser;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.exception.ScriptSyntaxException;
import org.junit.Test;

import java.util.*;

import static com.github.chengyuxing.common.script.lexer.FlowControlLexer.FI;
import static com.github.chengyuxing.common.script.lexer.FlowControlLexer.IF;
import static com.github.chengyuxing.common.utils.StringUtil.*;

public class IfParser {
    @Test
    public void test1() {
        String sql = "#if :b <> blank\n" +
                "    and t.b = :b\n" +
                "#fi\n" +
                "#if :c <> blank\n" +
                "    #if :c1 <> blank\n" +
                "        and t.c1 = :c1\n" +
                "        #if :cc1 <> blank\n" +
                "            and t.cc1 = :cc1\n" +
                "        #fi\n" +
                "        #if :cc2 <> blank\n" +
                "            and t.cc2 = :cc2\n" +
                "        #fi\n" +
                "    #fi\n" +
                "    #if :c2 <> blank\n" +
                "        and t.c2 = :c2\n" +
                "    #fi\n" +
                "    and cc = :cc\n" +
                "#fi";
        DataRow dataRow = DataRow.of(
                "b", "null",
                "c", "cyx",
                "c1", "aaa",
                "cc1", "aaa",
                "cc2", "aaa",
                "c2", "aaa"
        );
        Condition condition = new Condition(dataRow, "");
        parser(sql, dataRow, condition);
//        String res = calc(condition);
//        System.out.println(res);
    }

//    public String calc(Condition root) {
//        StringJoiner sb = new StringJoiner("\n");
//        sb.add(String.join("\n", root.getPrefix()));
//        boolean res;
//        if (root.getExpression().equals("")) {
//            res = true;
//        } else {
////            FastExpression expression = new FastExpression(root.getExpression().trim().substring(3));
////            res = expression.calc(root.getArgs());
//        }
//        if (res) {
//            sb.add(String.join("\n", root.getContent()));
//            for (Condition condition : root.getChild()) {
//                sb.add(calc(condition));
//            }
//        }
//        sb.add(String.join("\n", root.getSuffix()));
//        return sb.toString();
//    }

    public void parser(String content, Map<String, Object> args, Condition condition) {
        String[] lines = content.split(NEW_LINE);
        for (int i = 0, j = lines.length; i < j; i++) {
            String outerLine = lines[i];
            String trimOuterLine = outerLine.trim();
            int count = 0;
            // 处理if表达式块
            if (startsWithIgnoreCase(trimOuterLine, IF)) {
                List<Condition> conditions = condition.getChild();
                conditions.add(new Condition(args, trimOuterLine));
                Condition last = conditions.get(conditions.size() - 1);
                count++;
                // 内循环推进游标，用来判断嵌套if表达式块
                while (++i < j) {
                    String line = lines[i];
                    String trimLine = line.trim();
                    if (startsWithIgnoreCase(trimLine, IF)) {
                        last.getContent().add(line);
                        count++;
                    } else if (startsWithIgnoreCase(trimLine, FI)) {
                        last.getContent().add(line);
                        count--;
                        if (count < 0) {
                            throw new ScriptSyntaxException("can not find pair of 'if-fi' block at line " + i);
                        }
                        // 说明此处已经达到了嵌套fi的末尾
                        if (count == 0) {
                            if (containsAllIgnoreCase(last.getContent().toString(), IF, FI)) {
                                parser(last.getContent().toString(), args, condition);
                            }
                            break;
                        } else {
                            last.getSuffix().add(line);
                        }
                    } else {
                        // 非表达式的部分sql需要保留
                        last.getContent().add(line);
                    }
                }
                if (count != 0) {
                    throw new ScriptSyntaxException("can not find pair of 'if-fi' block at line " + i);
                }
            }
        }
    }
}
