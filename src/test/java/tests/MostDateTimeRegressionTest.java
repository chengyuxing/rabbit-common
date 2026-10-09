package tests;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.common.util.ValueUtils;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.*;
import java.util.Date;

import static org.junit.Assert.*;

public class MostDateTimeRegressionTest {
    @Test
    public void chineseTimesReadTheMatchedChineseFields() {
        assertEquals(LocalDateTime.of(2026, 10, 9, 12, 34, 56),
                MostDateTime.parse("2026年10月9日 12时34分56秒").toLocalDateTime());
        assertEquals(LocalTime.of(12, 34), MostDateTime.parse("2026年10月9日 12点34分").toLocalTime());
    }

    @Test
    public void everyFractionWidthRetainsPrecisionAndIsoOffset() {
        String digits = "123456789";
        for (int length = 1; length <= 9; length++) {
            String fraction = digits.substring(0, length);
            int nanos = Integer.parseInt((fraction + "000000000").substring(0, 9));
            String local = "2026-10-09 12:34:56." + fraction;
            assertEquals(nanos, MostDateTime.of(local).toLocalTime().getNano());
            assertEquals(nanos, MostDateTime.parse(local).toLocalTime().getNano());
            String iso = "2026-10-09T12:34:56." + fraction + "Z";
            Instant expected = OffsetDateTime.of(2026, 10, 9, 12, 34, 56, nanos, ZoneOffset.UTC).toInstant();
            assertEquals(expected, MostDateTime.of(iso).toInstant());
            assertEquals(expected, MostDateTime.parse(iso).toInstant());
            assertEquals(expected, MostDateTime.createISODateTime(iso).toZonedDateTime().toInstant());
        }
    }

    @Test
    public void compactDatesValidateCalendarAndTimeFieldsStrictly() {
        assertEquals(LocalDate.of(2024, 2, 29), MostDateTime.parse("20240229").toLocalDate());
        assertEquals(LocalDateTime.of(2026, 10, 9, 12, 34, 56),
                MostDateTime.parse("20261009123456").toLocalDateTime());
        assertEquals(123000000, MostDateTime.parse("20261009123456123").toLocalTime().getNano());
        assertEquals(Instant.ofEpochSecond(1791549296L), MostDateTime.parse("1791549296").toInstant());
        assertEquals(Instant.ofEpochMilli(1791549296123L), MostDateTime.parse("1791549296123").toInstant());
        assertEquals(LocalDate.of(2026, 10, 9), MostDateTime.parse("2026/10/09").toLocalDate());
        assertEquals(LocalDate.of(2026, 10, 9), MostDateTime.parse("2026.10.09").toLocalDate());
        for (String invalid : new String[]{"20260230", "20260230123456", "20260230123456123", "20261009240000"}) {
            reject(() -> MostDateTime.parse(invalid));
            reject(() -> MostDateTime.of(invalid));
        }
    }

    @Test
    public void standardIsoZonesAndLegacyCompactOffsetsRemainSupported() {
        assertEquals(Instant.parse("2026-10-09T04:34:56.123456789Z"),
                MostDateTime.parse("2026-10-09T12:34:56.123456789+0800").toInstant());
        assertEquals(ZoneId.of("Asia/Shanghai"), MostDateTime.parse(
                "2026-10-09T12:34:56+08:00[Asia/Shanghai]").getZonedDateTime().getZone());
        assertEquals(Instant.parse("2026-10-09T12:34:00Z"), MostDateTime.parse("2026-10-09T12:34Z").toInstant());
        assertEquals(ZoneOffset.ofHours(-8), MostDateTime.parse("2019-09-26T03:45:36.656-08").getZonedDateTime().getZone());
        assertEquals(ZoneOffset.ofHoursMinutesSeconds(-8, 0, -53),
                MostDateTime.parse("2019-09-26T03:45:36.656-080053").getZonedDateTime().getZone());
    }

    @Test
    public void rfcMonthNamesAreCaseInsensitive() {
        assertEquals(MostDateTime.parse("Wed Jan 04 18:52:01 CST 2023").toInstant(),
                MostDateTime.parse("wed jan 04 18:52:01 cst 2023").toInstant());
        assertEquals(Instant.parse("2023-01-04T09:36:48Z"),
                MostDateTime.parse("wed, 04 jan 2023 09:36:48 GMT").toInstant());
        assertEquals(Instant.parse("2023-01-04T09:36:48Z"),
                MostDateTime.parse("wed JAN 04 2023 17:36:48 GMT+0800").toInstant());
        reject(() -> MostDateTime.parse("Sat, 30 Feb 2026 09:36:48 GMT"));
    }

    @Test
    public void explicitPatternsPreserveZonesAndTreatQuotedLettersAsLiterals() {
        assertEquals(Instant.parse("2026-10-09T12:34:56Z"),
                MostDateTime.of("2026-10-09 12:34:56 +00:00", "yyyy-MM-dd HH:mm:ss XXX").toInstant());
        assertEquals(LocalDate.of(2026, 10, 9), MostDateTime.of("2026-10-09 H", "yyyy-MM-dd 'H'").toLocalDate());
        assertEquals(LocalTime.of(12, 34), MostDateTime.of("12:34", "HH:mm").toLocalTime());
        assertEquals(ZoneOffset.UTC, MostDateTime.of("2026-10-09 +00:00", "yyyy-MM-dd XXX")
                .getZonedDateTime().getZone());
        assertEquals(ZoneId.of("Pacific/Kiritimati"), MostDateTime.of("12:34 Pacific/Kiritimati", "HH:mm VV")
                .getZonedDateTime().getZone());
        assertEquals(ZoneId.of("Asia/Shanghai"), MostDateTime.of("2026-10-09 12:34:56 Asia/Shanghai",
                "yyyy-MM-dd HH:mm:ss VV").getZonedDateTime().getZone());
        reject(() -> MostDateTime.of("2026-02-30", "yyyy-MM-dd"));
        reject(() -> MostDateTime.of("2026-10-09 24:00", "yyyy-MM-dd HH:mm"));
    }

    @Test
    public void explicitZonesConvertInstantsWhileDefaultOverloadsKeepSourceZones() {
        ZonedDateTime source = ZonedDateTime.parse("2026-10-09T12:34:56+08:00[Asia/Shanghai]");
        for (java.time.temporal.Temporal value : new java.time.temporal.Temporal[]{source, source.toOffsetDateTime()}) {
            ZonedDateTime converted = MostDateTime.of(value, ZoneId.of("UTC")).getZonedDateTime();
            assertEquals(source.toInstant(), converted.toInstant());
            assertEquals(ZoneId.of("UTC"), converted.getZone());
        }
        assertEquals(source, MostDateTime.of(source).getZonedDateTime());
        assertEquals(source.getOffset(), MostDateTime.of(source.toOffsetDateTime()).getZonedDateTime().getZone());
        assertEquals(source.getZone(), MostDateTime.of(source).plus(1, java.time.temporal.ChronoUnit.DAYS)
                .getZonedDateTime().getZone());
    }

    @Test
    public void timeOnlyValuesUseTodayInTheirOwnZone() {
        for (ZoneId zone : new ZoneId[]{ZoneOffset.ofHours(-12), ZoneOffset.ofHours(14)}) {
            LocalDate before = LocalDate.now(zone);
            MostDateTime value = MostDateTime.of(LocalTime.NOON, zone);
            LocalDate after = LocalDate.now(zone);
            assertTrue(value.toLocalDate().equals(before) || value.toLocalDate().equals(after));
            assertEquals(LocalTime.NOON, value.toLocalTime());
        }
        LocalDate before = LocalDate.now();
        MostDateTime value = MostDateTime.parse("12:34:56");
        assertTrue(value.toLocalDate().equals(before) || value.toLocalDate().equals(LocalDate.now()));
        OffsetTime time = OffsetTime.of(12, 34, 56, 0, ZoneOffset.ofHours(14));
        ZonedDateTime expected = time.atDate(LocalDate.now(time.getOffset())).toZonedDateTime();
        assertEquals(expected.toInstant(), MostDateTime.of(time).toInstant());
        assertEquals(expected.withZoneSameInstant(ZoneId.of("UTC")),
                MostDateTime.of(time, ZoneId.of("UTC")).getZonedDateTime());
    }

    @Test
    public void strictParsingRejectsUnconsumedTextAndLegacyExtractionStillWorks() {
        for (String invalid : new String[]{"bad 2026-10-09 garbage", "2026-10-09T12:34:56Z junk",
                "2026-10-09 12:34:56.1234567890", "2026-10-09 12:34:56 +08:00",
                "2026-10-09 2026-10-10", "", "nonsense", "2026-02-30", "25:00:00"}) {
            reject(() -> MostDateTime.parse(invalid));
        }
        assertEquals(LocalDate.of(2001, 12, 21), MostDateTime.of("决定书二〇〇一年十二月二十一日的").toLocalDate());
        assertEquals(LocalDate.of(2026, 10, 9), MostDateTime.of("bad 2026-10-09 garbage").toLocalDate());
        assertEquals(LocalDate.of(2026, 6, 26), MostDateTime.parse("二〇二六年六月二十六日").toLocalDate());
    }

    @Test
    public void valueAdaptationUsesStrictParsingAndRetainsTimestampAndTemporalNanoseconds() {
        String source = "2026-10-09T12:34:56.123456789Z";
        Instant expected = Instant.parse(source);
        assertEquals(expected, ValueUtils.adaptValue(Instant.class, source));
        assertEquals(expected, ValueUtils.adaptValue(Timestamp.class, source).toInstant());
        assertEquals(123456789, ValueUtils.adaptValue(LocalDateTime.class, source).getNano());
        assertEquals(expected.toEpochMilli(), ValueUtils.adaptValue(Date.class, source).getTime());
        reject(() -> ValueUtils.adaptValue(Date.class, "prefix 2026-10-09"));
        reject(() -> ValueUtils.adaptValue(LocalDateTime.class, "2026-10-09 suffix"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void legacyAccessorStillReturnsLocalDateTime() {
        MostDateTime source = MostDateTime.parse("2026-10-09T12:34:56Z");
        assertEquals(source.toInstant(), source.getZonedDateTime().toInstant());
    }

    private static void reject(Runnable action) {
        try {
            action.run();
            fail("Expected an invalid date-time to be rejected");
        } catch (DateTimeException | IllegalArgumentException expected) {
            // A date/zone validation error or unrecognized input is expected.
        }
    }
}
