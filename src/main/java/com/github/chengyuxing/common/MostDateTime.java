package com.github.chengyuxing.common;

import com.github.chengyuxing.common.util.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The MostDateTime class provides a comprehensive set of functionalities for handling date and time.
 * It supports various operations such as conversion between different date-time formats, adding or subtracting
 * temporal units, and comparison. This class is designed to be flexible, allowing creation from multiple sources
 * including LocalDateTime, Date, String, and timestamp values, and can convert its state into several other
 * Java date-time types.
 */
public final class MostDateTime {
    // language=regexp
    public static final Pattern DATE_PATTERN = Pattern.compile("((?<y>\\d{4})[-/.年])?(?<m>\\d{1,2})[-/.月](?<d>\\d{1,2})日?");
    // language=regexp
    public static final Pattern EN_TIME_PATTERN = Pattern.compile("(?<h>\\d{1,2}):(?<m>\\d{1,2})(:(?<s>\\d{1,2})(\\.(?<n>\\d{3,9}))?)?");
    // language=regexp
    public static final Pattern ZH_TIME_PATTERN = Pattern.compile("((?<h>\\d{1,2})[时点])((?<m>\\d{1,2})分)((?<s>\\d{1,2})秒)?");
    // language=regexp
    public static final Pattern ISO_DATE_TIME_PATTERN = Pattern.compile("(?<date>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3,9})?)(?<zone>Z|GMT|UTC|UT|([+-](\\d{1,6}|\\d{2}:\\d{2}(:\\d{2})?)))?", Pattern.CASE_INSENSITIVE);
    // language=regexp
    public static final Pattern RFC_1123_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun),\\s+\\d{1,2}\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}\\s+GMT", Pattern.CASE_INSENSITIVE);
    // language=regexp
    public static final Pattern RFC_CST_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s+(?<M>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(?<d>\\d{1,2})\\s+(?<time>\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+CST\\s+(?<y>\\d{4})", Pattern.CASE_INSENSITIVE);
    // language=regexp
    public static final Pattern RFC_GMT_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s+(?<M>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(?<d>\\d{1,2})\\s+(?<y>\\d{4})\\s+(?<time>\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+GMT(?<zone>Z|GMT|UTC|UT|([+-](\\d{1,6}|\\d{2}:\\d{2}(:\\d{2})?)))", Pattern.CASE_INSENSITIVE);
    public static final Map<String, Integer> Mon = new HashMap<String, Integer>() {{
        put("Jan", 1);
        put("Feb", 2);
        put("Mar", 3);
        put("Apr", 4);
        put("May", 5);
        put("Jun", 6);
        put("Jul", 7);
        put("Aug", 8);
        put("Sep", 9);
        put("Oct", 10);
        put("Nov", 11);
        put("Dec", 12);
    }};
    private final LocalDateTime dateTime;

    /**
     * Constructs a new MostDateTime with temporal.
     *
     * @param dateTime dateTime
     */
    MostDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns a new MostDateTime with temporal.
     *
     * @param temporal temporal
     * @param zoneId   zoneId
     * @return MostDateTime instance
     */
    @Contract("null, _ -> fail")
    public static @NotNull MostDateTime of(Temporal temporal, ZoneId zoneId) {
        if (temporal instanceof LocalDateTime) {
            return new MostDateTime((LocalDateTime) temporal);
        }
        if (temporal instanceof LocalDate) {
            return new MostDateTime(((LocalDate) temporal).atStartOfDay());
        }
        if (temporal instanceof OffsetDateTime) {
            return new MostDateTime(((OffsetDateTime) temporal).toLocalDateTime());
        }
        if (temporal instanceof ZonedDateTime) {
            return new MostDateTime(((ZonedDateTime) temporal).toLocalDateTime());
        }
        if (temporal instanceof LocalTime) {
            return new MostDateTime(((LocalTime) temporal).atDate(LocalDate.now()));
        }
        if (temporal instanceof OffsetTime) {
            return new MostDateTime(((OffsetTime) temporal).atDate(LocalDate.now()).toLocalDateTime());
        }
        if (temporal instanceof Instant) {
            return new MostDateTime(((Instant) temporal).atZone(zoneId).toLocalDateTime());
        }
        throw new IllegalArgumentException("Unsupported temporal type: " + temporal.getClass());
    }

    /**
     * Returns a new MostDateTime with temporal.
     *
     * @param temporal temporal
     * @return MostDateTime instance
     */
    @Contract("null -> fail")
    public static @NotNull MostDateTime of(Temporal temporal) {
        return of(temporal, ZoneId.systemDefault());
    }

    /**
     * Returns a new MostDateTime with date.
     *
     * @param date   date
     * @param zoneId zoneId
     * @return MostDateTime instance
     */
    @Contract("_, _ -> new")
    public static @NotNull MostDateTime of(@NotNull Date date, ZoneId zoneId) {
        return new MostDateTime(date.toInstant().atZone(zoneId).toLocalDateTime());
    }

    /**
     * Returns a new MostDateTime with date.
     *
     * @param date date
     * @return MostDateTime instance
     */
    @Contract("_ -> new")
    public static @NotNull MostDateTime of(Date date) {
        return of(date, ZoneId.systemDefault());
    }

    /**
     * Returns a new MostDateTime with string datetime.
     *
     * @param datetime string datetime
     * @return MostDateTime instance
     * @see #toLocalDateTime(String)
     */
    public static @NotNull MostDateTime of(String datetime) {
        LocalDateTime ldt = toLocalDateTime(datetime);
        return of(ldt);
    }

    /**
     * Returns a new MostDateTime with string datetime and specific pattern.
     *
     * @param datetime string datetime
     * @param pattern  datetime pattern
     * @return MostDateTime instance
     */
    public static @NotNull MostDateTime of(String datetime, String pattern) {
        LocalDateTime ldt = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern(pattern));
        return of(ldt);
    }

    /**
     * Returns a new MostDateTime with timestamp.
     *
     * @param date timestamp
     * @return MostDateTime instance
     */
    @Contract("_ -> new")
    public static @NotNull MostDateTime of(long date) {
        return of(new Date(date));
    }

    /**
     * Minus the amount of current datetime temporal unit.
     *
     * @param amount the amount of the specified unit to subtract, may be negative
     * @param unit   the unit of the amount to subtract, not null
     * @return a new MostDateTime
     * @see java.time.temporal.ChronoUnit ChronoUnit
     */
    public @NotNull MostDateTime minus(long amount, TemporalUnit unit) {
        return of(dateTime.minus(amount, unit));
    }

    /**
     * Plus the amount of current datetime temporal unit.
     *
     * @param amount the amount of the specified unit to add, may be negative
     * @param unit   the unit of the amount to add, not null
     * @return a new MostDateTime
     * @see java.time.temporal.ChronoUnit ChronoUnit
     */
    public @NotNull MostDateTime plus(long amount, TemporalUnit unit) {
        return of(dateTime.plus(amount, unit));
    }

    /**
     * Gets the value of the specified field.
     *
     * @param field datetime part field name, not null
     * @return the value of the field
     * @see ChronoField
     */
    public int get(TemporalField field) {
        return dateTime.get(field);
    }

    /**
     * Compares this date-time to another date-time.
     *
     * @param other the other date-time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    public int compareTo(@NotNull MostDateTime other) {
        return dateTime.compareTo(other.dateTime);
    }

    /**
     * Convert to Instant.
     *
     * @param zoneId zoneId
     * @return a new Instant
     */
    public Instant toInstant(ZoneId zoneId) {
        return dateTime.atZone(zoneId).toInstant();
    }

    /**
     * Convert to Instant.
     *
     * @return a new Instant
     */
    public Instant toInstant() {
        return toInstant(ZoneId.systemDefault());
    }

    /**
     * Convert to Date.
     *
     * @param zoneId zoneId
     * @return a new Date
     */
    public Date toDate(ZoneId zoneId) {
        return new Date(toInstant(zoneId).toEpochMilli());
    }

    /**
     * Convert to Date.
     *
     * @return a new Date
     */
    public Date toDate() {
        return toDate(ZoneId.systemDefault());
    }

    /**
     * Convert to LocalDateTime.
     *
     * @return a new LocalDateTime
     */
    public LocalDateTime toLocalDateTime() {
        return dateTime;
    }

    /**
     * Convert to LocalDate.
     *
     * @return a new LocalDate
     */
    public LocalDate toLocalDate() {
        return dateTime.toLocalDate();
    }

    /**
     * Convert to LocalTime.
     *
     * @return a new LocalTime
     */
    public LocalTime toLocalTime() {
        return dateTime.toLocalTime();
    }

    /**
     * Format to string datetime.
     *
     * @param format format
     * @return string datetime
     */
    public @NotNull String toString(@NotNull String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format.trim()));
    }

    @Override
    public String toString() {
        if (dateTime == null) {
            return "";
        }
        return this.dateTime.toString();
    }

    /**
     * Convert string to local datetime object.<br>
     * formats：
     * <ul>
     *     <li>13 bit timestamp</li>
     *     <li>10 bit timestamp</li>
     *     <li>yyyyMMddHHmmssSSS</li>
     *     <li>yyyyMMddHHmmss</li>
     *     <li>yyyyMMdd</li>
     *     <li>yyyy[-/年]MM[-/月]dd[日]</li>
     *     <li>yyyy年MM月dd日 HH[时点]mm分ss秒</li>
     *     <li>yyyy[-/]MM[-/]dd HH:mm:ss.[SSS|ffffff|nnnnnnnnn]</li>
     *     <li>ISO, e.g. 2019-09-26T03:45:36.656+0800</li>
     *     <li>RFC_1123, e.g. Wed, 04 Jan 2023 09:36:48 GMT</li>
     *     <li>RFC-like, e.g. Wed Jan 04 2023 17:36:48 GMT+0800</li>
     *     <li>RFC-like, e.g. Wed Jan 04 18:52:01 CST 2023</li>
     * </ul>
     *
     * @param datetime string datetime
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(@NotNull String datetime) {
        datetime = datetime.trim();
        boolean isDigit = StringUtils.isDigit(datetime);
        int len = datetime.length();
        if (isDigit) {
            if (len == 17) {
                return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            }
            if (len == 14) {
                return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
            if (len == 8) {
                return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            if (len == 13) {
                return Instant.ofEpochMilli(Long.parseLong(datetime)).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            if (len == 10) {
                return Instant.ofEpochSecond(Long.parseLong(datetime)).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        }
        ISODateTime isoDateTime = createISODateTime(datetime);
        if (isoDateTime.isMatch()) {
            return isoDateTime.toLocalDateTime();
        }

        if (RFC_1123_DATE_TIME_PATTERN.matcher(datetime).matches()) {
            return LocalDateTime.parse(datetime, DateTimeFormatter.RFC_1123_DATE_TIME);
        }

        RFCLikeDate rfcLikeDate = createRFCLikeDateTime(datetime);
        if (rfcLikeDate.isMatch()) {
            return rfcLikeDate.toLocalDateTime();
        }

        boolean anyMatch = false;
        int year;
        int month = 1, day = 1, hour = 0, minus = 0, second = 0, nanoSeconds = 0;
        Matcher dateMatcher = DATE_PATTERN.matcher(datetime);
        if (dateMatcher.find()) {
            anyMatch = true;
            if (dateMatcher.group("y") != null) {
                year = Integer.parseInt(dateMatcher.group("y"));
            } else {
                year = LocalDateTime.now().getYear();
            }
            month = Integer.parseInt(dateMatcher.group("m"));
            day = Integer.parseInt(dateMatcher.group("d"));
        } else {
            year = LocalDateTime.now().getYear();
        }

        Matcher timeMatcher = EN_TIME_PATTERN.matcher(datetime);
        if (timeMatcher.find()) {
            anyMatch = true;
            hour = Integer.parseInt(timeMatcher.group("h"));
            minus = Integer.parseInt(timeMatcher.group("m"));
            if (timeMatcher.group("s") != null) {
                second = Integer.parseInt(timeMatcher.group("s"));
            }
            if (timeMatcher.group("n") != null) {
                String n = timeMatcher.group("n");
                int nLen = n.length();
                int ns = Integer.parseInt(n);
                if (nLen == 3) {
                    // milliseconds
                    nanoSeconds = ns * 1_000_000;
                } else if (nLen == 6) {
                    // microseconds
                    nanoSeconds = ns * 1000;
                } else if (nLen == 9) {
                    // nanoseconds
                    nanoSeconds = ns;
                }
            }
        } else {
            Matcher zhTimeMatcher = ZH_TIME_PATTERN.matcher(datetime);
            if (zhTimeMatcher.find()) {
                anyMatch = true;
                hour = Integer.parseInt(timeMatcher.group("h"));
                minus = Integer.parseInt(timeMatcher.group("m"));
                if (timeMatcher.group("s") != null) {
                    second = Integer.parseInt(timeMatcher.group("s"));
                }
            }
        }
        if (anyMatch) {
            return LocalDateTime.of(year, month, day, hour, minus, second, nanoSeconds);
        }
        throw new IllegalArgumentException("unknown date time format." + datetime);
    }

    /**
     * Create ISO datetime object.
     *
     * @param datetime e.g. 2019-09-26T03:45:36.656+0800
     * @return ISO datetime object
     */
    public static ISODateTime createISODateTime(String datetime) {
        return new ISODateTime(datetime);
    }

    /**
     * Create RFC-like datetime object.
     *
     * @param datetime e.g. Wed Jan 04 2023 17:36:48 GMT+0800
     * @return RFC-like datetime object
     */
    public static RFCLikeDate createRFCLikeDateTime(String datetime) {
        return new RFCLikeDate(datetime);
    }

    /**
     * UTC datetime.
     */
    public static class ISODateTime {
        private boolean match = false;
        private String date;
        private ZoneId zoneId;

        public ISODateTime(String stringDate) {
            Matcher m = ISO_DATE_TIME_PATTERN.matcher(stringDate.trim());
            if (m.matches()) {
                match = true;
                date = m.group("date");
                String zone = m.group("zone");
                if (zone == null) {
                    zoneId = ZoneId.systemDefault();
                    return;
                }
                zoneId = ZoneId.of(zone.toUpperCase());
            }
        }

        public LocalDateTime toLocalDateTime() {
            return LocalDateTime.parse(date).atZone(zoneId).toLocalDateTime();
        }

        public String getDate() {
            return date;
        }

        public ZoneId getZoneId() {
            return zoneId;
        }

        public boolean isMatch() {
            return match;
        }

        @Override
        public String toString() {
            return "ISODateTime{" +
                    "date='" + date + '\'' +
                    ", zoneId=" + zoneId +
                    '}';
        }
    }

    /**
     * RFC-like date.
     */
    public static class RFCLikeDate {
        private boolean match = false;
        private Integer year;
        private Integer month;
        private Integer day;
        private String time;
        private ZoneId zoneId;

        public RFCLikeDate(String stringDate) {
            stringDate = stringDate.trim();
            Matcher rfcCSTm = RFC_CST_DATE_TIME_PATTERN.matcher(stringDate);
            if (rfcCSTm.matches()) {
                match = true;
                year = Integer.parseInt(rfcCSTm.group("y"));
                month = Mon.get(rfcCSTm.group("M"));
                day = Integer.parseInt(rfcCSTm.group("d"));
                time = rfcCSTm.group("time");
                zoneId = ZoneId.systemDefault();
                return;
            }
            Matcher rfcGMTm = RFC_GMT_DATE_TIME_PATTERN.matcher(stringDate);
            if (rfcGMTm.matches()) {
                match = true;
                year = Integer.parseInt(rfcGMTm.group("y"));
                month = Mon.get(rfcGMTm.group("M"));
                day = Integer.parseInt(rfcGMTm.group("d"));
                time = rfcGMTm.group("time");
                zoneId = ZoneId.of(rfcGMTm.group("zone").toUpperCase());
            }
        }

        public LocalDateTime toLocalDateTime() {
            LocalDate localDate = LocalDate.of(year, month, day);
            LocalTime localTime = LocalTime.parse(time);
            return LocalDateTime.of(localDate, localTime).atZone(zoneId).toLocalDateTime();
        }

        public boolean isMatch() {
            return match;
        }

        public Integer getYear() {
            return year;
        }

        public Integer getMonth() {
            return month;
        }

        public Integer getDay() {
            return day;
        }

        public String getTime() {
            return time;
        }

        public ZoneId getZoneId() {
            return zoneId;
        }

        @Override
        public String toString() {
            return "RFCLikeDate{" +
                    "year=" + year +
                    ", month=" + month +
                    ", day=" + day +
                    ", time='" + time + '\'' +
                    ", zoneId=" + zoneId +
                    '}';
        }
    }

    /**
     * Convert string to local date object.
     *
     * @param datetime string datetime
     * @return local date
     */
    public static LocalDate toLocalDate(String datetime) {
        return toLocalDateTime(datetime).toLocalDate();
    }

    /**
     * Convert string to local time object.
     *
     * @param datetime string datetime
     * @return local time
     */
    public static LocalTime toLocalTime(String datetime) {
        return toLocalDateTime(datetime).toLocalTime();
    }

    /**
     * Convert string to instant object.
     *
     * @param datetime string datetime
     * @param zoneId   zoneId
     * @return instant
     */
    public static Instant toInstant(String datetime, ZoneId zoneId) {
        return toLocalDateTime(datetime).atZone(zoneId).toInstant();
    }

    /**
     * Convert string to instant object.
     *
     * @param datetime string datetime
     * @return instant
     */
    public static Instant toInstant(String datetime) {
        return toInstant(datetime, ZoneId.systemDefault());
    }

    /**
     * Convert string to timestamp.
     *
     * @param datetime string datetime
     * @param zoneId   zoneId
     * @return timestamp
     */
    public static long toEpochMilli(String datetime, ZoneId zoneId) {
        return toInstant(datetime, zoneId).toEpochMilli();
    }

    /**
     * Convert string to timestamp.
     *
     * @param datetime string datetime
     * @return timestamp
     */
    public static long toEpochMilli(String datetime) {
        return toInstant(datetime).toEpochMilli();
    }

    /**
     * Convert string to date object.
     *
     * @param datetime string datetime
     * @param zoneId   zoneId
     * @return date
     */
    public static Date toDate(String datetime, ZoneId zoneId) {
        return new Date(toEpochMilli(datetime, zoneId));
    }

    /**
     * Convert string to date object.
     *
     * @param datetime string datetime
     * @return date
     */
    public static Date toDate(String datetime) {
        return new Date(toEpochMilli(datetime));
    }

    /**
     * Get now of local datetime.
     *
     * @return 当前时间
     */
    public static MostDateTime now() {
        return of(LocalDateTime.now());
    }

    /**
     * Get current timestamp.
     *
     * @return timestamp
     */
    public static long currentTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
