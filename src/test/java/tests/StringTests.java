package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.StringFormatter;
import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.io.TypedProperties;
import com.github.chengyuxing.common.script.Comparators;
import com.github.chengyuxing.common.script.IPipe;
import com.github.chengyuxing.common.script.SimpleScriptParser;
import com.github.chengyuxing.common.script.impl.FastExpression;
import com.github.chengyuxing.common.tuple.Pair;
import com.github.chengyuxing.common.utils.CollectionUtil;
import com.github.chengyuxing.common.utils.ObjectUtil;
import com.github.chengyuxing.common.utils.StringUtil;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.chengyuxing.common.utils.StringUtil.FMT;
import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

public class StringTests {
    static String sql = "${   a   } ${!a.d} insert into ${  Table  } ${tables.fields} values (${  VALUES.1.f }), (${values.0}), (${  Values   })${b}";

    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("Table", "test.user");
        args.put("values", Arrays.asList("a", "b", "c"));
        args.put("VALUES", Arrays.asList(DataRow.fromPair("f", "c,d,f"), DataRow.fromPair("f", "x,v,z")));
        args.put("a", LocalDateTime.now());
        args.put("a.d", "LocalDateTime.now()");
        args.put("tables", DataRow.fromPair("fields", "id,name,age"));

        String s = new FileResource("file:/Users/chengyuxing/Downloads/bbb.sql").readString(StandardCharsets.UTF_8);

        String res = (FMT.format(s, args));
        Files.write(Paths.get("/Users/chengyuxing/Downloads/ccc.sql"), res.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testOut() throws IOException {
        int i = 0;
        List<String> list = new ArrayList<>();
        while (++i < 2000000) {
            list.add("\"${   a   } ${!a.d} insert into ${  Table  } ${tables.fields} values (${  VALUES.1.f }), (${values.0}), (${  Values   })${b}\\n\"");
        }
        Files.write(Paths.get("/Users/chengyuxing/Downloads/bbb.sql"), list);
    }

    @Test
    public void testq2() {
        Map<String, Object> args = new HashMap<>();
        args.put("Table", "test.user");
        args.put("values", Arrays.asList("a", "b", "c"));
        args.put("VALUES", Arrays.asList(DataRow.fromPair("f", "c,d,f"), DataRow.fromPair("f", "x,v,z")));
        args.put("a", LocalDateTime.now());
        args.put("a.d", "LocalDateTime.now()");
        args.put("tables", DataRow.fromPair("fields", "id,name,age"));
        String res = new StringFormatter().format(sql, args);
        System.out.println(res);
    }

    @Test
    public void test6() throws Exception {
        String str = "${ user } <> blank && ${ user.name } !~ 'j'";
        System.out.println(FMT.format(str, DataRow.fromPair("user", ":user", "user.name", ":user.name")));
    }

    @Test
    public void testa() {
        Matcher m = Pattern.compile("(?<out>\\$\\{\\s*(?<key>!?[\\w._-]+)\\s*})").matcher("select ${ fields } from test.user where ${  cnd} and id in (${!idArr}) or id = ${!idArr.1}");
        while (m.find()) {
            System.out.println(m.group("out") + ":" + m.group("key"));
        }
    }

    @Test
    public void testb() {
        StringBuffer buffer = new StringBuffer("a${name}b");
        System.out.println(buffer.replace(1, 8, "1234567890"));
    }

    @Test
    public void testForExp() {
        Map<String, Object> map = new HashMap<>();
        map.put("user.id", ":user.id");
        System.out.println(FMT.format("${user.id} <> blank", map));
    }

    @Test
    public void test2() throws Exception {
        String str = "insert into ${ table } ${fields} id, name, age values (${ values.data.1 }), (${values.data.1})";
        Object values = DataRow.fromPair("data", Arrays.asList("1,2,3", "4,5,6"));

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
        resource.transferTo(Files.newOutputStream(Paths.get("/Users/chengyuxing/Downloads/bbb.tar.gz")));
    }

    @Test
    public void test1113() throws IOException {
        FileResource resource = new FileResource("file:/Users/chengyuxing/Downloads/flex.html");
        System.out.println(resource.readString(StandardCharsets.UTF_8));
    }

    @Test
    public void test341() {
        setValue(new byte[0]);
    }

    public static void setValue(Object i) {
        System.out.println(i.getClass().getName());
        System.out.println(i instanceof Byte[]);
    }

    @Test
    public void testT() {
        String s = new FileResource("b.txt").readString(StandardCharsets.UTF_8);
        System.out.println(s.replace("\\n", "\n"));
    }

    @Test
    public void testExp() {
        SimpleScriptParser simpleScriptParser = new SimpleScriptParser() {
            @Override
            protected String trimExpression(String line) {
                String tl = line.trim();
                if (tl.startsWith("--")) {
                    String s = tl.substring(2).trim();
                    if (s.startsWith("#")) {
                        return s;
                    }
                }
                return line;
            }

            @Override
            protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, List<String> body, Map<String, Object> args) {
                body.removeIf(l -> {
                    String tl = l.trim();
                    return tl.startsWith("--") && !tl.substring(2).trim().startsWith("#");
                });
                String formatted = FMT.format(String.join(NEW_LINE, body), args);
                if (varName != null) {
                    String varParam = "_for." + forVarKey(varName, forIndex, varIndex);
                    formatted = formatted.replace("_for." + varName, varParam);
                }
                if (idxName != null) {
                    String idxParam = "_for." + forVarKey(idxName, forIndex, varIndex);
                    formatted = formatted.replace("_for." + idxName, idxParam);
                }
                return formatted;
            }
        };

        DataRow d = DataRow.fromPair(
                "c", "blank",
                "c1", "blank",
                "c2", "blank",
                "data", DataRow.fromPair(
                        "id", 12,
                        "name", "chengyuxing",
                        "age", 30,
                        "address", "昆明市"
                ),
                "ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8),
                "list", Arrays.asList(
                        "A",
                        "B",
                        DataRow.fromPair(
                                "nums",
                                Arrays.asList("1", "2", "3")),
                        "D",
                        "E")
        );

        String s = new FileResource("me.sql").readString(StandardCharsets.UTF_8);

        String res = simpleScriptParser.parse(s, d);
        System.out.println(res);

        Map<String, Object> vars = simpleScriptParser.getForVars();
        System.out.println(vars);
    }

    @Test
    public void testDeepValue() {
        DataRow r = DataRow.fromPair("_for",
                DataRow.fromPair("pair_6_3", Pair.of("name", "chengyuxing"))
        );
        Object value = ObjectUtil.getDeepValue(r, "_for.pair_6_3.item2");
        System.out.println(value);
    }

    @Test
    public void testPipe() {
        DataRow row = DataRow.fromPair(
                "id", 12,
                "name", "chengyuxing",
                "age", 30,
                "address", "昆明市"
        );
        System.out.println(new IPipe.Map2Pairs().transform(row));
    }

    @Test
    public void testPipePattern() {
        System.out.println("|".matches(FastExpression.PIPES_PATTERN));
    }
}
