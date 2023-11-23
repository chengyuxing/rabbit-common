package com.github.chengyuxing.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Datetime util.
 */
public final class DateTimes {
    //language=RegExp
    public static final Pattern DATE_PATTERN = Pattern.compile("(?<y>\\d{4}[-/年])?(?<m>\\d{1,2})[-/月](?<d>\\d{1,2})日?");
    //language=RegExp
    public static final Pattern TIME_PATTERN = Pattern.compile("(?<h>\\d{1,2})[:时点](?<m>\\d{1,2})(?<s>[:分]\\d{1,2})?秒?");
    //language=RegExp
    public static final Pattern ISO_DATE_TIME_PATTERN = Pattern.compile("(?<date>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?)(?<zone>[zZ]|([+-](\\d{1,6}|\\d{2}:\\d{2}(:\\d{2})?)))?");
    //language=RegExp
    public static final Pattern RFC_1123_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun),\\s+\\d{1,2}\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2} GMT");
    //language=RegExp
    public static final Pattern RFC_CST_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s+(?<M>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(?<d>\\d{1,2})\\s+(?<time>\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+CST\\s+(?<y>\\d{4})");
    //language=RegExp
    public static final Pattern RFC_GMT_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\\s+(?<M>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(?<d>\\d{1,2})\\s+(?<y>\\d{4})\\s+(?<time>\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+GMT(?<zone>[zZ]|([+-](\\d{1,6}|\\d{2}:\\d{2}(:\\d{2})?)))");
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
    private final Temporal temporal;

    /**
     * Constructs a new DateTimes with temporal.
     *
     * @param temporal temporal
     */
    DateTimes(Temporal temporal) {
        this.temporal = temporal;
    }

    /**
     * Returns a new DateTimes with temporal.
     *
     * @param temporal temporal
     * @return DateTimes instance
     */
    public static DateTimes of(Temporal temporal) {
        return new DateTimes(temporal);
    }

    /**
     * Returns a new DateTimes with date.
     *
     * @param date date
     * @return DateTimes instance
     */
    public static DateTimes of(Date date) {
        return new DateTimes(date.toInstant());
    }

    /**
     * Returns a new DateTimes with string datetime.
     *
     * @param datetime string datetime
     * @return DateTimes instance
     */
    public static DateTimes of(String datetime) {
        LocalDateTime ldt = toLocalDateTime(datetime);
        return of(ldt);
    }

    /**
     * Format to string datetime.
     *
     * @param format format
     * @return string datetime
     */
    public String toString(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format.trim());
        if (temporal instanceof LocalTime) {
            return ((LocalTime) temporal).format(formatter);
        }
        if (temporal instanceof LocalDateTime) {
            return ((LocalDateTime) temporal).format(formatter);
        }
        if (temporal instanceof LocalDate) {
            return ((LocalDate) temporal).format(formatter);
        }
        if (temporal instanceof Instant) {
            return ((Instant) temporal).atZone(ZoneId.systemDefault()).format(formatter);
        }
        if (temporal instanceof ZonedDateTime) {
            return ((ZonedDateTime) temporal).toLocalDateTime().format(formatter);
        }
        if (temporal instanceof OffsetDateTime) {
            return ((OffsetDateTime) temporal).toLocalDateTime().format(formatter);
        }
        if (temporal instanceof OffsetTime) {
            return ((OffsetTime) temporal).toLocalTime().format(formatter);
        }
        throw new UnsupportedOperationException("type " + temporal.getClass().getTypeName() + "is not support currently.");
    }

    @Override
    public String toString() {
        if (temporal == null) {
            return "";
        }
        return this.temporal.toString();
    }

    /**
     * Convert string to local datetime object.<br>
     * formats：
     * <ul>
     *     <li>13 bit timestamp</li>
     *     <li>10 bit timestamp</li>
     *     <li>yyyyMMddHHmmss</li>
     *     <li>yyyyMMdd</li>
     *     <li>yyyy[-/年]MM[-/月]dd[日]</li>
     *     <li>yyyy[-/年]MM[-/月]dd[日] HH[:时点]mm[:分]ss[秒]</li>
     *     <li>ISO, e.g. 2019-09-26T03:45:36.656+0800</li>
     *     <li>RFC_1123, e.g. Wed, 04 Jan 2023 09:36:48 GMT</li>
     *     <li>RFC-like, e.g. Wed Jan 04 2023 17:36:48 GMT+0800</li>
     *     <li>RFC-like, e.g. Wed Jan 04 18:52:01 CST 2023</li>
     * </ul>
     *
     * @param datetime string datetime
     * @return LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(String datetime) {
        datetime = datetime.trim().replaceAll("\\s+", " ");
        if (datetime.matches("\\d{14}")) {
            return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }

        if (datetime.matches("\\d{8}")) {
            return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        if (datetime.matches("\\d{13}")) {
            return Instant.ofEpochMilli(Long.parseLong(datetime)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        if (datetime.matches("\\d{10}")) {
            return Instant.ofEpochSecond(Long.parseLong(datetime)).atZone(ZoneId.systemDefault()).toLocalDateTime();
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

        int year;
        int month = 1, day = 1, hour = -1, minus = 0, second = 0;
        Matcher dateMatcher = DATE_PATTERN.matcher(datetime);
        if (dateMatcher.find()) {
            hour = 0;
            if (dateMatcher.group("y") != null) {
                year = Integer.parseInt(dateMatcher.group("y").substring(0, 4));
            } else {
                year = LocalDateTime.now().getYear();
            }
            month = Integer.parseInt(dateMatcher.group("m"));
            day = Integer.parseInt(dateMatcher.group("d"));
        } else {
            year = LocalDateTime.now().getYear();
        }
        Matcher timeMatcher = TIME_PATTERN.matcher(datetime);
        if (timeMatcher.find()) {
            hour = Integer.parseInt(timeMatcher.group("h"));
            minus = Integer.parseInt(timeMatcher.group("m"));
            if (timeMatcher.group("s") != null) {
                second = Integer.parseInt(timeMatcher.group("s").substring(1));
            }
        }
        if (hour != -1) {
            return LocalDateTime.of(year, month, day, hour, minus, second);
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
                if ("z".equals(zone)) {
                    zone = "Z";
                }
                zoneId = ZoneId.of(zone);
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
                zoneId = ZoneId.of(rfcGMTm.group("zone"));
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
     * @return instant
     */
    public static Instant toInstant(String datetime) {
        return toLocalDateTime(datetime).atZone(ZoneId.systemDefault()).toInstant();
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
    public static DateTimes now() {
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

    /**
     * JSON {@link Date date}/{@link Temporal java8-date} type formatter.
     *
     * @param format date format
     * @return formatted string date
     */
    public static Function<Object, Object> dateFormatter(String format) {
        return v -> {
            if (v instanceof Temporal) {
                return of((Temporal) v).toString(format);
            }
            if (v instanceof Date) {
                return of((Date) v).toString(format);
            }
            return v;
        };
    }

    /**
     * JSON {@link Date date}/{@link Temporal java8-date} type formatter(yyyy-MM-dd HH:mm:ss).
     */
    public static final Function<Object, Object> NORMAL_DATE_FORMATTER = dateFormatter("yyyy-MM-dd HH:mm:ss");
}
