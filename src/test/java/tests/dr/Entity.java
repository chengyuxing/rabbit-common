package tests.dr;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public class Entity {
    private Date dt;
    private LocalDateTime ldt;
    private Integer id;
    private Map<String,Object> pg;


    @Override
    public String toString() {
        return "Entity{" +
                "dt=" + dt +
                ", ldt=" + ldt +
                ", id=" + id +
                ", pg=" + pg +
                '}';
    }

    public Map<String, Object> getPg() {
        return pg;
    }

    public void setPg(Map<String, Object> pg) {
        this.pg = pg;
    }

    public Date getDt() {
        return dt;
    }

    public void setDt(Date dt) {
        this.dt = dt;
    }

    public LocalDateTime getLdt() {
        return ldt;
    }

    public void setLdt(LocalDateTime ldt) {
        this.ldt = ldt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
