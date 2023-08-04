package tests.dr;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.utils.ObjectUtil;
import org.junit.Test;
import org.postgresql.util.PGobject;
import tests.entity.Coord;

import java.time.LocalDateTime;
import java.util.Date;

public class DataRowTests {
    @Test
    public void test1() throws Exception {
        Entity en = new Entity();
        en.setDt(new Date());
        en.setLdt(LocalDateTime.now());

        System.out.println(DataRow.ofEntity(en).toMap());
    }

    @Test
    public void test2() throws Exception {
        DataRow dr = DataRow.of("dt", "2021-12-23",
                "ldt", LocalDateTime.now(),
                "id", 12);
        System.out.println(dr);
        System.out.println(dr.toEntity(Entity.class));
    }

    @Test
    public void pgObj() throws Exception {
        PGobject pgobj = new PGobject();
        pgobj.setType("jsonb");
        pgobj.setValue("{\"name\":\"cyx\"}");
        DataRow dr = DataRow.of("pg", pgobj);
        System.out.println(dr.toEntity(Entity.class));
    }

    @Test
    public void testsqlDt() throws Exception {
        System.out.println(new Date().getTime());
        java.sql.Date ts = new java.sql.Date(1635138231902L);
        System.out.println(ts.getTime());
    }

    @Test
    public void testIn() {
        DataRow dataRow = DataRow.of("x", 10, "y", 20);
        Coord coord = dataRow.toEntity(Coord.class, dataRow.get("x"), dataRow.get("y"));
        System.out.println(coord);
        System.out.println(ObjectUtil.getValue(coord, "x"));
    }
}
