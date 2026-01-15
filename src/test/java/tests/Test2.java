package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.common.util.ValueUtils;
import com.github.chengyuxing.common.util.StringUtils;
import org.junit.Test;
import tests.entity.DateEntity;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class Test2 {

    static List<DataRow> rows = new ArrayList<>();

    //    @BeforeClass
    @Test
    public void init() {
        for (int i = 0; i < 10000; i++) {
            DataRow row = DataRow.of(
                    new String[]{"a", "b", "c", "d", "e"},
                    new Object[]{1, 2, 3, 4, 5}
            );
            rows.add(row);
        }
    }

    public static class Now {
        private Date a;
        private LocalDateTime b;

        @Override
        public String toString() {
            return "Now{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }

        public LocalDateTime getB() {
            return b;
        }

        public void setB(LocalDateTime b) {
            this.b = b;
        }

        public Date getA() {
            return a;
        }

        public void setA(Date a) {
            this.a = a;
        }
    }

    @Test
    public void testdd() throws Exception {
        DataRow row = DataRow.of("a", "2022-12-23", "b", "2022-12-01");
        System.out.println(row.toEntity(Now.class));
        System.out.println(row.toKeyValue());
    }

    @Test
    public void mp() throws Exception {
        DataRow row = DataRow.zip(Arrays.asList(
                DataRow.of("name", "cyx", "age", 217),
                DataRow.of("name", "cydx", "age", 247),
                DataRow.of("name", "cyxx", "age", 27),
                DataRow.of("name", "cyx", "age", 257)
        ));
        System.out.println(row);
    }

    @Test
    public void d1() throws Exception {
        DataRow row = DataRow.of(
                new String[]{"a", "b", "c", "d", "e"},
                new Object[]{null, 2, "12", 4, 5}
        );
        System.out.println(row.getInt("k"));
    }

    @Test
    public void rTest() throws Exception {
        System.out.println(rows.get(111).getString("c"));
    }

    @Test
    public void speedTest() throws Exception {
        List<Map<String, Object>> maps = rows.stream().collect(Collectors.toList());
        System.out.println(maps.size());
    }

    @Test
    public void fileTest() throws Exception {
        FileResource resource = new FileResource("file:/Users/chengyuxing/Downloads/Bob.app.zip");
        System.out.println(resource.getURL());
    }

    @Test
    public void dtTest() throws Exception {
        System.out.println(MostDateTime.toLocalDateTime("20210201092132999"));
        System.out.println("20210201092100".length());
    }

    @Test
    public void testDt2() throws Exception {
        System.out.println(MostDateTime.createISODateTime("2019-09-26T03:45:36.656Z"));
        System.out.println(MostDateTime.createISODateTime("2019-09-26T03:45:36.656-0800"));
        System.out.println(MostDateTime.createISODateTime("2019-09-25T18:00:14"));
        System.out.println(MostDateTime.createISODateTime("2019-09-25T18:00:14z"));
        System.out.println(111);
        System.out.println(MostDateTime.toLocalDateTime("2019-09-25 18:00:14.999"));
        System.out.println(222);
        System.out.println(LocalDateTime.parse("2019-09-25T18:00:14.1"));
        System.out.println(333);

        System.out.println(LocalDateTime.now());

        System.out.println(MostDateTime.toLocalDateTime("2019-09-26T03:45:36.656-0800"));
        System.out.println(MostDateTime.currentTimestamp());

        System.out.println(MostDateTime.toLocalDateTime("2021年12月23日"));

        System.out.println(MostDateTime.toLocalDateTime("Wed, 04 Jan 2023 09:36:48 GMT"));

        System.out.println(MostDateTime.toLocalDateTime("Wed Jan 04 18:52:01 CST 2023"));
        System.out.println(MostDateTime.toLocalDateTime("Wed Jan 04 2023 17:36:48 GMT+0800"));

        System.out.println(MostDateTime.createRFCLikeDateTime("Wed Jan 04 2023 17:36:48 GMT+0800"));

        System.out.println(new Date());


    }

    @Test
    public void test23() {
        System.out.println(MostDateTime.of("2019-09-26"));
        System.out.println(MostDateTime.of("2019-09-26T03:45:36.656-08"));
        System.out.println(MostDateTime.of("2019-09-26T03:45:36.656-080053").toString("yyyy-MM-dd hh:mm:ss"));
        System.out.println(ZoneId.of("+08:00:54"));
    }

    @Test
    public void testUrl() throws MalformedURLException {
        URL url = new URL("https://github.com/chengyuxing/sqlc/releases/download/2.1.2/sqlc-v2.1.2.tar.gz");
        System.out.println(url.getFile());
        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             ByteArrayOutputStream arr = new ByteArrayOutputStream();
             BufferedOutputStream out = new BufferedOutputStream(arr)) {
            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
            byte[] bytes = arr.toByteArray();
            System.out.println(FileResource.formatFileSize(bytes.length));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test24() {
        System.out.println(MostDateTime.of("2022.12.13_11.55.15", "yyyy.MM.dd_HH.mm.ss"));
        System.out.println(MostDateTime.now().compareTo(MostDateTime.of("2025/12/13 11:55:15")));
    }

    @Test
    public void testMostDateTime() throws Exception {
        MostDateTime mostDateTime = MostDateTime.now();
        MostDateTime another = mostDateTime.plus(3, ChronoUnit.MINUTES);
        System.out.println(mostDateTime);
        System.out.println(another);
        System.out.println(another.get(ChronoField.MONTH_OF_YEAR));
        System.out.println(another.toDate());
        System.out.println(another.toInstant());
        System.out.println(another.toLocalDateTime());
    }

    @Test
    public void testdtM() throws Exception {
    }

    @Test
    public void concat() throws Exception {
        DataRow row1 = DataRow.of("a", 1, "b", 2);
        DataRow row2 = DataRow.of("c", 11, "d", 23);

        System.out.println(row1);
    }

    @Test
    public void removeElement() throws Exception {
        DataRow row = DataRow.of("a", 1, "b", 2, "c", 3, "d", 4, "e", 5);
        System.out.println(row);
        System.out.println(row.remove("b"));
        System.out.println(row.put("c", 109));
    }

    @Test
    public void StrTests() throws Exception {
        System.out.println(StringUtils.startsWithIgnoreCase("Select * from Test.user", "SELECT"));
        System.out.println(FileResource.getFileExtension("wwww.docx"));
    }

    @Test
    public void sss() throws Exception {
        String str = ";;;;;;;;]\n  \t\r   Select * from Test.user;;;\t   ;;";
//        System.out.println(StringUtil.trimStarts(str, ";", "\n", " ", "]", "\t", "\r"));
    }

    @Test
    public void IdxOfI() throws Exception {
        String str = "Select * from Test.user";
        System.out.println(StringUtils.containsIgnoreCase(str, "TEST"));
        System.out.println(StringUtils.charEqualIgnoreCase('S', 's'));
        System.out.println(!StringUtils.containsAllIgnoreCase(str, "from", "user"));
        System.out.println(StringUtils.containsIgnoreCase(str, "我的"));
    }

    @Test
    public void sdfg() throws Exception {
        DataRow dataRow = DataRow.of("name", "cyx", "age", 27, "address", "kunming");

        LinkedHashMap<String, Object> map = dataRow.reduce(new LinkedHashMap<>(), (acc, name, value) -> {
            acc.put(name, value);
            return acc;
        });

        System.out.println(map);
    }

    @Test
    public void testArgs() throws Exception {
        DataRow dataRow = DataRow.of("id", 2, "name", "cyx", "dt", MostDateTime.now().toString("yyyy-MM-dd"));
//        String json = Jackson.toJson(dataRow);
//        System.out.println(json);
//
//        System.out.println(Jackson.toObject(json, DataRow.class));

        Map<String, Object> map = DataRow.of("a", 1);
    }

    @Test
    public void testLocalDateJson() {
        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeDeserializer dateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTimeSerializer dateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        module.addDeserializer(LocalDateTime.class, dateTimeDeserializer);
        module.addSerializer(LocalDateTime.class, dateTimeSerializer);

//        System.out.println(Jackson.toJson(DataRow.of("now", LocalDateTime.now())));
    }

    @Test
    public void testVarArgs() {
        a(new JavaTimeModule());
    }

    public static void a(Module... modules) {
        System.out.println(modules.getClass().getName());
        Module[] modules1 = new Module[]{new JavaTimeModule()};
        System.out.println(modules1.getClass().getName());
    }

    @Test
    public void testlMap() throws Exception {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("c", 1);
        map.put("b", 1);
        map.put("a", 1);
        map.put("d", 1);
        System.out.println(map);
        map.put("a", 2);
        map.put("d", 21);
        map.put("e", 34);
        System.out.println(map);
    }

    @Test
    public void jacksonTest() throws JsonProcessingException {
        String json = "[{\"name\":\"cyx\",\"age\":28}]";
        ObjectMapper mapper = new ObjectMapper();
        List<User> users = mapper.readerForListOf(User.class)
                .readValue(json);
        System.out.println(users);
//        System.out.println(Jackson.toObject(json, List.class));
//        System.out.println(Jackson.toObjects(json, User.class));
    }

    @Test
    public void testDt() {
        DataRow row = DataRow.of("now", "2022-12-23T09:54:00", "dt", new Date());
        DateEntity dateEntity = ValueUtils.mapToEntity(row, DateEntity.class);
        System.out.println(dateEntity);

        Instant instant = ValueUtils.toTemporal(Instant.class, new Date());
        System.out.println(instant);
    }

    @Test
    public void testRow() {
        DataRow row = DataRow.of("now", MostDateTime.toLocalDateTime("2022-12-23"), "age", 30);
        Integer age = row.getAs("0", null, 29);
        System.out.println(age);
        System.out.println(row.getFirstAs(LocalDateTime.now()));
        System.out.println(row.getFirst());
        System.out.println(row.getInt("age", 100));
    }
}
