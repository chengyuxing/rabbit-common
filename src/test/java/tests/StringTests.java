package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.io.TypedProperties;
import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.utils.CollectionUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    public void test23() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "cyx");
        map.put("address", "kunming");
        Map<String, Object> user = new HashMap<>();
        user.put("user", user);
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
    public void test89() {
        Path path = Paths.get("/Users/chengyuxing/Downloads/flatlaf-demo-3.0.jar");
        System.out.println(Files.isDirectory(path));
        System.out.println(Files.exists(path));
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
        ClassPathResource resource = new ClassPathResource("my.xql");
        System.out.println(resource.exists());
        System.out.println(resource.getFileName());
        System.out.println(resource.getFilenameExtension());
        System.out.println(resource.getPath());
        System.out.println(resource.getURL());
        System.out.println(resource.getLastModified());
        System.out.println(resource.getInputStream().available());
    }

    @Test
    public void test8() throws IOException {
        TypedProperties properties = new TypedProperties();
        properties.load(new FileResource("file:/Users/chengyuxing/Downloads/xql-file-manager.properties").getInputStream());
        System.out.println(properties.getList("filenames", Collections.emptyList()));
        System.out.println(properties.getMap("files", Collections.emptyMap()));
    }

    @Test
    public void test9() {
        System.out.println(Comparators.equals("", "\"\""));
        System.out.println(Comparators.regexPass(true, "\"\\w+\"", false));
    }

    @Test
    public void test10() {
        System.out.println(Comparators.compare(null, "==", Comparators.valueOf(null)));
    }

    @Test
    public void test11() {
        Object v = Comparators.valueOf("'abc'");
        System.out.println(Comparators.compare("'abc'", "@", "^'\\w+'"));
        System.out.println(v);
        System.out.println(v.getClass());
    }

    @Test
    public void test12() {
        IPipe<?> pipe = new IPipe.Length();
        Map<String, IPipe<?>> pipes1 = new HashMap<>();
        pipes1.put("a", pipe);
        Map<String, IPipe<?>> pipes2 = new HashMap<>();
        pipes2.put("a", pipe);

        System.out.println(new HashMap<>(pipes1).equals(pipes2));
    }

    @Test
    public void test34() {
        for (AtomicInteger i = new AtomicInteger(0); i.get() < 10; i.getAndIncrement()) {
            inc(i);
            System.out.println(i);
        }
    }

    @Test
    public void test35() {
        System.out.println(StringUtil.replaceIgnoreCase("abc", "o", "D"));
    }

    public static void inc(AtomicInteger i) {
        i.incrementAndGet();
    }

    @Test
    public void test112() throws IOException {
        FileResource resource = new FileResource("file:/Users/chengyuxing/Downloads/zulu8.68.0.21-ca-jdk8.0.362-macosx_aarch64.tar.gz");
//        System.out.println(resource.readString());
//        System.out.println(resource.readBytes().length);
        resource.write(Files.newOutputStream(Paths.get("/Users/chengyuxing/Downloads/bbb.tar.gz")));
    }

    @Test
    public void test1113() throws IOException {
        FileResource resource = new FileResource("file:/Users/chengyuxing/Downloads/flex.html");
        System.out.println(resource.readString());
    }
}
