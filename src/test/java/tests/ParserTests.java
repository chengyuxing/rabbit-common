package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.script.ast.IElement;
import com.github.chengyuxing.common.script.ast.ScriptAst;
import com.github.chengyuxing.common.script.ast.impl.*;
import com.github.chengyuxing.common.script.lexer.RabbitScriptLexer;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ParserTests {

    @Test
    public void test1() {
        FileResource fr = new FileResource("flow-control/parser_if.txt");
        RabbitScriptLexer lexer = new RabbitScriptLexer(fr.readString(StandardCharsets.UTF_8));
        RabbitScriptParser parser = new RabbitScriptParser(lexer.tokenize());
        List<IElement> ast = parser.parse();
        System.out.println(ast);
    }

    @Test
    public void test2() {
        FileResource fr = new FileResource("flow-control/parser_if2.txt");
        RabbitScriptLexer lexer = new RabbitScriptLexer(fr.readString(StandardCharsets.UTF_8));
        RabbitScriptParser parser = new RabbitScriptParser(lexer.tokenize());
        List<IElement> ast = parser.parse();

        EvalContext context = new EvalContext(DataRow.of("age", 52));

        RabbitScriptEvaluator evaluator = new RabbitScriptEvaluator(context);
        EvalResult res = evaluator.execute(new ScriptAst(ast));
        System.out.println(res);
    }
}