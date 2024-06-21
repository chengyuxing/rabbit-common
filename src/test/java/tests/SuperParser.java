package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.expression.IExpression;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.SimpleScriptParser;
import com.github.chengyuxing.common.script.expression.impl.FastExpression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SuperParser extends SimpleScriptParser {

    @Override
    public String trimExpression(String line) {
        if (line.startsWith("[") && line.endsWith("]")) {
            return line.substring(2, line.length() - 1);
        }
        return line;
    }

    @Override
    public IExpression expression(String expression) {
        FastExpression fastExpression = new FastExpression(expression);
        Map<String, IPipe<?>> pipeMap = new HashMap<>();
        pipeMap.put("top5", (IPipe<String>) value -> value.toString().substring(0, 5));
        fastExpression.setPipes(pipeMap);
        return fastExpression;
    }

    public static void main(String[] args) throws IOException {
        String content = Files.lines(Paths.get("/Users/chengyuxing/IdeaProjects/rabbit-common/src/test/resources/a.txt"))
                .collect(Collectors.joining("\n"));
        SimpleScriptParser parser = new SuperParser();
        String res = parser.parse(content, DataRow.of("id", "-90", "name", "abcdefrgjgh"));
        System.out.println(res);
    }
}
