package tests;

import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

public class StringTests {
    @Test
    public void test() throws Exception{
        String sql = "${a}insert into ${Table} $fields values (${VALUES}), (${values}), (${Values})${b}";
        System.out.println(StringUtil.replaceIgnoreCase(sql,"${values}","cyx"));
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${table}", "test.user"));
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${A}", "?"));
        System.out.println(StringUtil.replaceIgnoreCase(sql, "${b}", "?"));
    }
}
