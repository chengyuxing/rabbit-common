package tests.dr;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

public class Entity {
    private Date dt;
    private LocalDateTime ldt;
    private Integer id;
    private Map<String,Object> pg;
    private String userId;


    @Override
    public String toString() {
        return "Entity{" +
                "dt=" + dt +
                ", ldt=" + ldt +
                ", id=" + id +
                ", pg=" + pg +
                ", userId='" + userId + '\'' +
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
