package tests.entity;

import java.time.Instant;
import java.time.LocalDateTime;

public class DateEntity {
    private Instant now;
    private LocalDateTime dt;

    @Override
    public String toString() {
        return "DateEntity{" +
                "now=" + now +
                ", dt=" + dt +
                '}';
    }

    public Instant getNow() {
        return now;
    }

    public void setNow(Instant now) {
        this.now = now;
    }

    public LocalDateTime getDt() {
        return dt;
    }

    public void setDt(LocalDateTime dt) {
        this.dt = dt;
    }
}
