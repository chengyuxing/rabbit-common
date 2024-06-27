package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.StringFormatter;
import com.github.chengyuxing.common.io.ClassPathResource;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.io.TypedProperties;
import com.github.chengyuxing.common.script.parser.FlowControlParser;
import com.github.chengyuxing.common.script.expression.Comparators;
import com.github.chengyuxing.common.script.expression.IPipe;
import com.github.chengyuxing.common.script.parser.SimpleParser;
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

import static com.github.chengyuxing.common.script.expression.Patterns.PIPES_PATTERN;
import static com.github.chengyuxing.common.utils.StringUtil.FMT;
import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

public class StringTests {
    static String sql = "${   a   } ${!a.d} insert into ${  Table  } ${tables.fields} values (${  VALUES.1.f }), (${values.0}), (${  Values   })${b}";

    @Test
    public void test() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("Table", "test.user");
        args.put("values", Arrays.asList("a", "b", "c"));
        args.put("VALUES", Arrays.asList(DataRow.of("f", "c,d,f"), DataRow.of("f", "x,v,z")));
        args.put("a", LocalDateTime.now());
        args.put("a.d", "LocalDateTime.now()");
        args.put("tables", DataRow.of("fields", "id,name,age"));

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
        args.put("VALUES", Arrays.asList(DataRow.of("f", "c,d,f"), DataRow.of("f", "x,v,z")));
        args.put("a", LocalDateTime.now());
        args.put("a.d", "LocalDateTime.now()");
        args.put("tables", DataRow.of("fields", "id,name,age"));
        String res = new StringFormatter().format(sql, args);
        System.out.println(res);
    }

    @Test
    public void test6() throws Exception {
        String str = "${ user } <> blank && ${ user.name } !~ 'j'";
        System.out.println(FMT.format(str, DataRow.of("user", ":user", "user.name", ":user.name")));
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
        Object values = DataRow.of("data", Arrays.asList("1,2,3", "4,5,6"));

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
        System.out.println(CollectionUtil.hasSameKeyIgnoreCase(DataRow.of("id", "1", "ID", "2")));
    }

    @Test
    public void test89() {
        Path path = Paths.get("/Users/chengyuxing/Downloads/flatlaf-demo-3.0.jar");
        System.out.println(Files.isDirectory(path));
        System.out.println(Files.exists(path));
    }

    @Test
    public void test3w() throws URISyntaxException, IOException {
        FileResource resource = new FileResource("file:///Users/chengyuxing/Downloads/rabbit-sql-plugin-2.3.1-signed.zip");
        System.out.println(resource.exists());
        System.out.println(resource.getFileName());
        System.out.println(resource.getFilenameExtension());
        System.out.println(resource.getPath());
        System.out.println(resource.getURL());
        System.out.println(resource.getLastModified());
        System.out.println(resource.getInputStream().available());
        System.out.println("--------");
        FileResource httpR = new FileResource("https://github.com/chengyuxing/sqlc/releases/download/2.1.2/sqlc-v2.1.2.tar.gz?id=13&dt=2021-12-12",
                DataRow.of(
                        "headers", DataRow.of("token", "abc"),
                        "connectTimeout", 0));
        System.out.println(httpR.exists());
        System.out.println(httpR.getFileName());
        System.out.println(httpR.getFilenameExtension());
        System.out.println(httpR.getPath());
        System.out.println(httpR.getURL());
        System.out.println(httpR.getLastModified());
        System.out.println(FileResource.formatFileSize(httpR.readBytes()));
        System.out.println("----");
        String s = new FileResource("https://github.com/chengyuxing/sqlc/raw/master/README.md").readString(StandardCharsets.UTF_8);
        System.out.println(s);
    }

    @Test
    public void testBaseHeader() {
        String headers = "Authorization:password;token:wksksalwooa";
        String base64 = Base64.getMimeEncoder().encodeToString(headers.getBytes(StandardCharsets.UTF_8));
        System.out.println(base64);
        System.out.println(new String(Base64.getMimeDecoder().decode(base64)));
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

        System.out.println(Comparators.compare("'  '", "=", Comparators.ValueType.BLANK));

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
        System.out.println(StringUtil.replaceIgnoreCase("abcO", "o", "D"));
    }

    public static void inc(AtomicInteger i) {
        i.incrementAndGet();
    }

    @Test
    public void test112() throws IOException {
        FileResource resource = new FileResource("file:/Users/chengyuxing/Downloads/jdk-17_linux-aarch64_bin.tar.gz");
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
        SimpleParser simpleScriptParser = new SimpleParser() {
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
            protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, String body, Map<String, Object> args) {
//                body.removeIf(l -> {
//                    String tl = l.trim();
//                    return tl.startsWith("--") && !tl.substring(2).trim().startsWith("#");
//                });
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

        DataRow d = DataRow.of(
                "c", "blank",
                "c1", "blank",
                "c2", "blank",
                "data", DataRow.of(
                        "name", "chengyuxing",
                        "age", 30,
                        "address", "昆明市"
                ),
                "ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8),
                "list", Arrays.asList(
                        "A",
                        "B",
                        DataRow.of(
                                "nums",
                                Arrays.asList("1", "2", "3")),
                        "D",
                        "E")
        );

        String s = new FileResource("me.sql").readString(StandardCharsets.UTF_8);

        String res = simpleScriptParser.parse(s, d);
        System.out.println(res);

        Map<String, Object> vars = simpleScriptParser.getForContextVars();
        System.out.println(vars);
        System.out.println(ObjectUtil.getDeepValue(vars, "item_6_0.value"));
    }

    @Test
    public void testNestIf() {
        String s = "if\n" +
                "    if\n" +
                "    else\n" +
                "       \n" +
                "    fi\n" +
                "else\n" +
                "    if\n" +
                "        if\n" +
                "            if\n" +
                "            else\n" +
                "            fi\n" +
                "        else\n" +
                "        fi\n" +
                "    fi\n" +
                "fi\n" +
                "\n" +
                "if\n" +
                "fi\n" +
                "\n" +
                "if\n" +
                "else\n" +
                "fi\n" +
                "\n";
        String[] ss = s.split("\n");
        int depth = 0;
        int elsePosition = -1;
        for (int i = 0; i < ss.length; i++) {
            String item = ss[i].trim();
            if (item.equals("if")) {
                depth++;
            } else if (item.equals("fi")) {
                depth--;
            } else if (item.equals("else") && depth == 1) {
                elsePosition = i;
            }
            if (depth == 0) {
                break;
            }
        }
        System.out.println(Arrays.toString(ss));
        System.out.println(elsePosition);
    }

    @Test
    public void testLexer() {
        String s = new FileResource("me.sql").readString(StandardCharsets.UTF_8);

        DataRow d = DataRow.of(
                "c", "blank",
                "c1", "blank",
                "c2", "blank",
                "data", DataRow.of(
                        "name", "chengyuxing",
                        "age", 30,
                        "address", "昆明市"
                ),
                "ids", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8),
                "list", Arrays.asList(
                        "A",
                        "B",
                        DataRow.of(
                                "nums",
                                Arrays.asList("1", "2", "3")),
                        "D",
                        "E")
        );

        FlowControlParser parser = new FlowControlParser(s) {
            public static final String FOR_VARS_KEY = "_for";
            public static final String VAR_PREFIX = FOR_VARS_KEY + ".";

            @Override
            protected String trimExpression(String line) {
                String tl = line.trim();
                if (tl.startsWith("--")) {
                    String ss = tl.substring(2).trim();
                    if (ss.startsWith("#")) {
                        return ss;
                    }
                }
                return line;
            }

            @Override
            protected String forLoopBodyFormatter(int forIndex, int varIndex, String varName, String idxName, String body, Map<String, Object> args) {
                String formatted = StringUtil.FMT.format(body, args);
                if (!varName.isEmpty()) {
                    String varParam = VAR_PREFIX + forVarKey(varName, forIndex, varIndex);
                    formatted = formatted.replace(VAR_PREFIX + varName, varParam);
                }
                if (!idxName.isEmpty()) {
                    String idxParam = VAR_PREFIX + forVarKey(idxName, forIndex, varIndex);
                    formatted = formatted.replace(VAR_PREFIX + idxName, idxParam);
                }
                return formatted;
            }
        };

        String res = parser.parse(d);
        System.out.println(res);

        Map<String, Object> vars = parser.getForContextVars();
        System.out.println(vars);
        System.out.println(ObjectUtil.getDeepValue(vars, "item_6_0.value"));
    }

    @Test
    public void testDeepValue() {
        DataRow r = DataRow.of(
                "_for", DataRow.of("pair_6_3", Pair.of("name", "chengyuxing"))
        );
        Object value = ObjectUtil.getDeepValue(r, "_for.pair_6_3.item2");
        System.out.println(value);
    }

    @Test
    public void testPipe() {
        DataRow row = DataRow.of(
                "id", 12,
                "name", "chengyuxing",
                "age", 30,
                "address", "昆明市"
        );
        System.out.println(new IPipe.Map2Pairs().transform(row));
    }

    @Test
    public void testPipePattern() {
        System.out.println("|".matches(PIPES_PATTERN));
    }

    @Test
    public void testW() {
        System.out.println("a_b_c".matches("\\w+"));
    }

    @Test
    public void rowTest() {
        System.out.println(DataRow.of("a", 1, "c", 2));
        System.out.println(DataRow.ofEntity(new User()));
//        System.out.println(DataRow.ofMap(new HashMap<>()));
        System.out.println(DataRow.of(new String[]{"name", "age"}, new Object[]{"chengyuxing", 28}));

        List<DataRow> rows = new ArrayList<>();
        rows.add(DataRow.of("a", 1, "b", "x"));
        rows.add(DataRow.of("a", 2, "b", "x1"));
        rows.add(DataRow.of("a", 3, "b", "x2"));
        rows.add(DataRow.of("a", 4, "b", "x3"));
        rows.add(DataRow.of("a", 5, "b", "x4"));

        System.out.println(rows.get(0).<Integer>getFirstAs());
        System.out.println(rows.get(0).<Integer>getAs(0));
        System.out.println(rows.get(0).<String>getAs("b"));

        System.out.println(DataRow.ofEntity(true));

        DataRow row = DataRow.of("name", "cyx");
        System.out.println(row);
        System.out.println(row.<Object>getAs(0));
        System.out.println(row.<String>getFirstAs());

        System.out.println(new DataRow(0));
    }

    @Test
    public void testR() {
        Pattern p = Pattern.compile("Z|GMT", Pattern.CASE_INSENSITIVE);
        System.out.println(p.matcher("gmt").matches());
    }

    @Test
    public void testJackson() {
        DataRow row = DataRow.of("now", LocalDateTime.now(), "current", new Date());
    }
}
