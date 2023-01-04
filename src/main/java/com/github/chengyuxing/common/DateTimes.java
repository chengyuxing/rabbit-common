package com.github.chengyuxing.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期时间工具类
 */
public class DateTimes {
    public static final Pattern DATE_PATTERN = Pattern.compile("(?<y>\\d{4}[-/年])?(?<m>\\d{1,2})[-/月](?<d>\\d{1,2})日?");
    public static final Pattern TIME_PATTERN = Pattern.compile("(?<h>\\d{1,2})[:时点](?<m>\\d{1,2})(?<s>[:分]\\d{1,2})?秒?");
    public static final Pattern UTC_DATE_TIME_PATTERN = Pattern.compile("(?<date>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?)(?<zone>[zZ]|[+-]\\d{4})?");
    public static final Pattern RFC_1123_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun), \\d{1,2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} GMT");
    public static final Pattern RFC_CST_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun) (?<M>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (?<d>\\d{1,2}) (?<time>\\d{1,2}:\\d{1,2}:\\d{1,2}) CST (?<y>\\d{4})");
    public static final Pattern RFC_GMT_DATE_TIME_PATTERN = Pattern.compile("(Mon|Tue|Wed|Thu|Fri|Sat|Sun) (?<M>Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) (?<d>\\d{1,2}) (?<y>\\d{4}) (?<time>\\d{1,2}:\\d{1,2}:\\d{1,2}) GMT(?<zone>[+-]\\d{4})");
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
     * 构造函数
     *
     * @param temporal 时间
     */
    DateTimes(Temporal temporal) {
        this.temporal = temporal;
    }

    /**
     * 时间工具
     *
     * @param temporal 时间
     * @return 时间工具实例
     */
    public static DateTimes of(Temporal temporal) {
        return new DateTimes(temporal);
    }

    /**
     * 时间工具
     *
     * @param date 时间
     * @return 时间工具实例
     */
    public static DateTimes of(Date date) {
        return new DateTimes(Instant.ofEpochMilli(date.getTime()));
    }

    /**
     * 格式化输出输出时间
     *
     * @param format 格式
     * @return 格式化的时间字符串
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

    /**
     * 将时间字符串转换为本地日期时间对象<br>
     * 支持的时间格式：
     * <ul>
     *     <li>13位时间戳</li>
     *     <li>10位时间戳</li>
     *     <li>yyyyMMddHHmmss</li>
     *     <li>yyyyMMdd</li>
     *     <li>yyyy[-/年]MM[-/月]dd[日]</li>
     *     <li>yyyy[-/年]MM[-/月]dd[日] HH[:时点]mm[:分]ss[秒]</li>
     *     <li>ISO时间格式，例如：2019-09-26T03:45:36.656+0800</li>
     *     <li>RFC_1123时间格式，例如：Wed, 04 Jan 2023 09:36:48 GMT</li>
     *     <li>类似RFC时间格式，例如：Wed Jan 04 2023 17:36:48 GMT+0800</li>
     *     <li>类似RFC时间格式，例如：Wed Jan 04 18:52:01 CST 2023</li>
     * </ul>
     *
     * @param s 时间字符串
     * @return 本地日期时间
     */
    public static LocalDateTime toLocalDateTime(String s) {
        s = s.trim().replaceAll("\\s+", " ");

        if (s.matches("\\d{14}")) {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }

        if (s.matches("\\d{8}")) {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        if (s.matches("\\d{13}")) {
            return Instant.ofEpochMilli(Long.parseLong(s)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        if (s.matches("\\d{10}")) {
            return Instant.ofEpochSecond(Long.parseLong(s)).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        UTCDate utcDate = createUTCDate(s);
        if (utcDate.isMatch()) {
            return utcDate.toLocalDateTime();
        }

        if (RFC_1123_DATE_TIME_PATTERN.matcher(s).matches()) {
            return LocalDateTime.parse(s, DateTimeFormatter.RFC_1123_DATE_TIME);
        }

        RFCLikeDate rfcLikeDate = createRFCLikeDate(s);
        if (rfcLikeDate.isMatch()) {
            return rfcLikeDate.toLocalDateTime();
        }

        int year;
        int month = 1, day = 1, hour = -1, minus = 0, second = 0;
        Matcher dateMatcher = DATE_PATTERN.matcher(s);
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
        Matcher timeMatcher = TIME_PATTERN.matcher(s);
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
        throw new IllegalArgumentException("un know date time format: " + s);
    }

    /**
     * UTC格式时间
     *
     * @param stringDate 例如：2019-09-26T03:45:36.656+0800
     * @return UTC格式时间对象
     */
    public static UTCDate createUTCDate(String stringDate) {
        return new UTCDate(stringDate);
    }

    /**
     * 近似RFC格式的时间
     *
     * @param stringDate 例如：Wed Jan 04 2023 17:36:48 GMT+0800
     * @return RFC格式时间对象
     */
    public static RFCLikeDate createRFCLikeDate(String stringDate) {
        return new RFCLikeDate(stringDate);
    }

    /**
     * UTC格式时间
     */
    public static class UTCDate {
        private boolean match = false;
        private String date;
        private ZoneId zoneId;

        public UTCDate(String stringDate) {
            Matcher m = UTC_DATE_TIME_PATTERN.matcher(stringDate.trim());
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
            return "IsoDateConvert{" +
                    "match=" + match +
                    ", date='" + date + '\'' +
                    ", zoneId=" + zoneId +
                    '}';
        }
    }

    /**
     * 近似RFC格式的时间
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
                    "match=" + match +
                    ", year=" + year +
                    ", month=" + month +
                    ", day=" + day +
                    ", time='" + time + '\'' +
                    ", zoneId=" + zoneId +
                    '}';
        }
    }

    /**
     * 将时间字符串转换为本地日期对象
     *
     * @param s 时间字符串
     * @return 本地日期
     */
    public static LocalDate toLocalDate(String s) {
        return toLocalDateTime(s).toLocalDate();
    }

    /**
     * 将时间字符串转换为本地时间对象
     *
     * @param s 时间字符串
     * @return 本地时间
     */
    public static LocalTime toLocalTime(String s) {
        return toLocalDateTime(s).toLocalTime();
    }

    /**
     * 将时间字符串转换为当前日期时间对象
     *
     * @param s 时间字符串
     * @return 当前日期时间
     */
    public static Instant toInstant(String s) {
        return toLocalDateTime(s).atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * 将时间字符串转为时间戳（毫秒数）
     *
     * @param s 时间字符串
     * @return 时间戳
     */
    public static long toEpochMilli(String s) {
        return toInstant(s).toEpochMilli();
    }

    /**
     * 将时间字符串转换为日期对象
     *
     * @param s 时间字符串
     * @return 日期
     */
    public static Date toDate(String s) {
        return new Date(toEpochMilli(s));
    }

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public static DateTimes now() {
        return of(LocalDateTime.now());
    }

    /**
     * 获取当前时间戳
     *
     * @return 当前时间戳
     */
    public static long currentTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
