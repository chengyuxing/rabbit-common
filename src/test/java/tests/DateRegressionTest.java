package tests;

import com.github.chengyuxing.common.DataRow;
import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.util.ValueUtils;
import org.junit.Test;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.Date;

import static org.junit.Assert.*;

public class DateRegressionTest {
    @Test
    public void jdbcDatesMappedToUtilDateHaveTheDeclaredRuntimeType() {
        for (Date source : new Date[]{java.sql.Date.valueOf("2026-10-09"),
                Time.valueOf("12:34:56"), Timestamp.valueOf("2026-10-09 12:34:56.123456789")}) {
            Date result = ValueUtils.adaptValue(Date.class, source);
            assertEquals(Date.class, result.getClass());
            assertEquals(source.getTime(), result.getTime());
            assertEquals(Instant.ofEpochMilli(source.getTime()), result.toInstant());
            assertNotSame(source, result);
            DateRow row = DataRow.of("value", source).toEntity(DateRow.class);
            assertEquals(Date.class, row.getValue().getClass());
        }
        Date plain = new Date();
        assertSame(plain, ValueUtils.adaptValue(Date.class, plain));
        assertNull(ValueUtils.adaptValue(Date.class, null));
    }

    @Test
    public void explicitJdbcTargetsKeepTheirTypesAndTimestampPrecision() {
        Timestamp timestamp = Timestamp.valueOf("2026-10-09 12:34:56.123456789");
        assertSame(timestamp, ValueUtils.adaptValue(Timestamp.class, timestamp));
        java.sql.Date date = java.sql.Date.valueOf("2026-10-09");
        Time time = Time.valueOf("12:34:56");
        assertSame(date, ValueUtils.adaptValue(java.sql.Date.class, date));
        assertSame(time, ValueUtils.adaptValue(Time.class, time));
        Date source = new Date(timestamp.getTime());
        assertEquals(source.getTime(), ValueUtils.adaptValue(java.sql.Date.class, source).getTime());
        assertEquals(source.getTime(), ValueUtils.adaptValue(Time.class, source).getTime());
        assertEquals(source.getTime(), ValueUtils.adaptValue(Timestamp.class, source).getTime());
        assertEquals(java.sql.Date.class, ValueUtils.adaptValue(java.sql.Date.class, "2026-10-09").getClass());
        assertEquals(Time.class, ValueUtils.adaptValue(Time.class, "2026-10-09 12:34:56").getClass());
        assertEquals(Timestamp.class, ValueUtils.adaptValue(Timestamp.class, "2026-10-09 12:34:56").getClass());
    }

    @Test
    public void sqlDatesAndTimesConvertWithoutUnsupportedToInstantCalls() {
        java.sql.Date date = java.sql.Date.valueOf("2026-10-09");
        Time time = Time.valueOf("12:34:56");
        ZoneId zone = ZoneId.of("Pacific/Honolulu");
        assertEquals(LocalDate.of(2026, 10, 9), ValueUtils.toTemporal(LocalDate.class, date, zone));
        assertEquals(LocalTime.of(12, 34, 56), ValueUtils.toTemporal(LocalTime.class, time, zone));
        for (Date source : new Date[]{date, time}) {
            Instant instant = Instant.ofEpochMilli(source.getTime());
            ZonedDateTime zoned = instant.atZone(zone);
            assertEquals(instant, ValueUtils.adaptValue(Instant.class, source));
            assertEquals(zoned, ValueUtils.toTemporal(ZonedDateTime.class, source, zone));
            assertEquals(zoned.toLocalDateTime(), ValueUtils.toTemporal(LocalDateTime.class, source, zone));
            assertEquals(zoned.toOffsetDateTime(), ValueUtils.toTemporal(OffsetDateTime.class, source, zone));
            assertEquals(zoned.toOffsetDateTime().toOffsetTime(), ValueUtils.toTemporal(OffsetTime.class, source, zone));
            assertEquals(instant, MostDateTime.of(source, zone).toInstant());
        }
    }

    @Test
    public void timestampToTemporalAndMostDateTimeRetainsNanoseconds() {
        Timestamp source = Timestamp.valueOf("2026-10-09 12:34:56.123456789");
        assertEquals(source.toInstant(), ValueUtils.adaptValue(Instant.class, source));
        assertEquals(source.toInstant(), MostDateTime.of(source).toInstant());
        assertEquals(123456789, ValueUtils.adaptValue(LocalDateTime.class, source).getNano());
    }

    public static class DateRow {
        private Date value;
        public Date getValue() { return value; }
        public void setValue(Date value) { this.value = value; }
    }
}
