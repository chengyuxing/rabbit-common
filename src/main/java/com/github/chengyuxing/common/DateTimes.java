package com.github.chengyuxing.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日期时间工具类
 */
public class DateTimes {
    static final Pattern DATE_PATTERN = Pattern.compile("(?<y>\\d{4}[-/年])?(?<m>\\d{1,2})[-/月](?<d>\\d{1,2})日?");
    static final Pattern TIME_PATTERN = Pattern.compile("(?<h>\\d{1,2})[:时点](?<m>\\d{1,2})(?<s>[:分]\\d{1,2})?秒?");
    static final Pattern ISO_DATE_TIME = Pattern.compile("(?<date>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,3})?)(?<zone>[zZ]|[+-]\\d{4})?");

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
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
        throw new UnsupportedOperationException("type " + temporal.getClass().getTypeName() + "is not support currently.");
    }

    /**
     * 将时间字符串转换为本地日期时间对象
     *
     * @param s 时间字符串
     * @return 本地日期时间
     */
    public static LocalDateTime toLocalDateTime(String s) {
        IsoDateConvert isoDateConvert = isoDateConvert(s);
        if (isoDateConvert.isIsoDate()) {
            return LocalDateTime.parse(isoDateConvert.getDate()).atZone(isoDateConvert.getZoneId()).toLocalDateTime();
        }
        if (s.matches("\\d{14}")) {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        if (s.matches("\\d{8}")) {
            return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd"));
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
        if (hour == -1) {
            throw new IllegalArgumentException("un know date time format: " + s);
        }
        return LocalDateTime.of(year, month, day, hour, minus, second);
    }

    public static IsoDateConvert isoDateConvert(String stringDate) {
        return new IsoDateConvert(stringDate);
    }

    public static class IsoDateConvert {
        private boolean isIsoDate = false;
        private String date;
        private ZoneId zoneId;

        public IsoDateConvert(String stringDate) {
            Matcher m = ISO_DATE_TIME.matcher(stringDate.trim());
            if (m.matches()) {
                isIsoDate = true;
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

        public String getDate() {
            return date;
        }

        public ZoneId getZoneId() {
            return zoneId;
        }

        public boolean isIsoDate() {
            return isIsoDate;
        }

        @Override
        public String toString() {
            return "IsoDateConvert{" +
                    "isIsoDate=" + isIsoDate +
                    ", date='" + date + '\'' +
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
