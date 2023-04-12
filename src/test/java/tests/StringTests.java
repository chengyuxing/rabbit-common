package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.utils.CollectionUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StringTests {
    static String sql = "${   a   } ${a.d} insert into ${  Table  } ${tables.fields} values (${  VALUES.1.f }), (${values.0}), (${  Values   })${b}";

    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("Table", "test.user");
        args.put("values", Arrays.asList("a", "b", "c"));
        args.put("VALUES", Arrays.asList(DataRow.fromPair("f", "c,d,f"), DataRow.fromPair("f", "x,v,z")));
        args.put("a", LocalDateTime.now());
        args.put("a.d", "LocalDateTime.now()");
        args.put("tables", DataRow.fromPair("fields", "id,name,age"));

        System.out.println(StringUtil.format(sql, args));
    }

    @Test
    public void test6() throws Exception {
        String str = "${ user } <> blank && ${ user.name } !~ 'j'";
        System.out.println(StringUtil.format(str, DataRow.fromPair("user", ":user", "user.name", ":user.name")));
    }

    @Test
    public void test2() throws Exception {
        String str = "insert into ${ table } ${fields} id, name, age values (${ values.data.1 }), (${values.data.1})";
        Object values = DataRow.fromPair("data", Arrays.asList("1,2,3", "4,5,6"));

        String res = StringUtil.format(str, "table", "test.user");
        String res2 = StringUtil.format(str, "values", values);
        System.out.println(res);
        System.out.println(res2);
    }

    @Test
    public void test3() throws Exception {
        System.out.println(StringUtil.format("${a.b}", "a", "b"));
    }

    @Test
    public void testx1() throws Exception {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_WEEK_DATE));
    }

    @Test
    public void testCol() throws Exception {
        System.out.println(CollectionUtil.hasSameKeyIgnoreCase(DataRow.fromPair("id", "1", "ID", "2")));
    }

    @Test
    public void test3w() throws URISyntaxException, IOException {
        FileResource resource = new FileResource("file:/Users/chengyuxing/Downloads/flatlaf-demo-3.0.jar");
        System.out.println(resource.exists());
        System.out.println(resource.getFileName());
        System.out.println(resource.getFilenameExtension());
        System.out.println(resource.getPath());
        System.out.println(resource.getURL());
        System.out.println(resource.getLastModified());
        System.out.println(resource.getInputStream().available());
    }

    @Test
    public void test3a() throws URISyntaxException, IOException {
        ClassPathResource resource = new ClassPathResource("mym.xql");
        System.out.println(resource.exists());
        System.out.println(resource.getFileName());
        System.out.println(resource.getFilenameExtension());
        System.out.println(resource.getPath());
        System.out.println(resource.getURL());
        System.out.println(resource.getLastModified());
        System.out.println(resource.getInputStream().available());
    }
}
