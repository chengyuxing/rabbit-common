package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.script.impl.FastExpression;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PipesTests {
    public static void main(String[] args) {
        FastExpression expression = FastExpression.of("ignore");
        new Thread(() -> System.out.println(expression.pipedValue("abcde", "|length"))).start();
        new Thread(() -> System.out.println(expression.pipedValue("ab", "|length"))).start();
        new Thread(() -> System.out.println(expression.pipedValue("abc", "|length"))).start();
        new Thread(() -> System.out.println(expression.pipedValue("abcd", "|length"))).start();
    }

    @Test
    public void test() throws Exception{
        FastExpression expression = FastExpression.of("ignore");
        System.out.println(expression.pipedValue("abc"," | upper"));
    }

    @Test
    public void test1() throws Exception {
        FastExpression expression = FastExpression.of(":idCard|is_idcard == true");
        Map<String, IPipe<?>> pipeMap = new ConcurrentHashMap<>();
        pipeMap.put("is_idcard", new IsIdCard());
        expression.setCustomPipes(pipeMap);
        System.out.println(expression.calc(DataRow.fromPair("idCard", "53011119930510000X")));

        FastExpression expression2 = FastExpression.of(":name | length > 3");
        System.out.println(expression2.calc(DataRow.fromPair("name", "cyx")));
    }

    @Test
    public void test4s() throws Exception {
        FastExpression exp = FastExpression.of("!(:id >= 0 || :name | length <= 3) && :age<=21");
        System.out.println(exp.calc(DataRow.fromPair("id", 10, "name", "cyx", "age", 13)));
    }

    static class IsIdCard implements IPipe<Boolean> {
        @Override
        public Boolean transform(Object value) {
            if (value == null) {
                return false;
            }
            System.out.println(value);
            return value.toString().matches("\\d{17}[xX]");
        }
    }
}
