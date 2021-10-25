package tests.dr;

import com.github.chengyuxing.common.DataRow;
import org.junit.Test;
import org.postgresql.util.PGobject;

import java.time.LocalDateTime;
import java.util.Date;

public class DataRowTests {
    @Test
    public void test1() throws Exception {
        Entity en = new Entity();
        en.setDt(new Date());
        en.setLdt(LocalDateTime.now());

        System.out.println(DataRow.fromEntity(en).toMap());
        System.out.println(new Date().getTime());
        System.out.println(new Date(1635132891314L));
    }

    @Test
    public void test2() throws Exception {
        DataRow dr = DataRow.fromPair("dt", "2021-12-23",
                "ldt", LocalDateTime.now(),
                "id", 12);
        dr.foreach((name, value) -> {
            System.out.println(dr.getType(name));
        });
        System.out.println(dr.toEntity(Entity.class));
    }

    @Test
    public void pgObj() throws Exception {
        PGobject pgobj = new PGobject();
        pgobj.setType("jsonb");
        pgobj.setValue("{\"name\":\"cyx\"}");
        DataRow dr = DataRow.fromPair("pg", pgobj);
        System.out.println(dr.toEntity(Entity.class));
    }

    @Test
    public void testsqlDt() throws Exception{
        System.out.println(new Date().getTime());
        java.sql.Date ts = new java.sql.Date(1635138231902L);
        System.out.println(ts.getTime());
    }
}
