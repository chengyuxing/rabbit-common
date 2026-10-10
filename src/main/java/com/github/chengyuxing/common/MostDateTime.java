package com.github.chengyuxing.common;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.common.util.ValueUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
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
    private static final Logger log = LoggerFactory.getLogger(MostDateTime.class);

    private static final Map<String, Integer> CC_NUMBERS = new HashMap<String, Integer>() {{
        put("一", 1);
        put("二", 2);
        put("三", 3);
        put("四", 4);
        put("五", 5);
        put("六", 6);
        put("七", 7);
        put("八", 8);
        put("九", 9);
        put("十", 10);
    }};
    private static final Map<String, Integer> CC_NUMBERS_WITH_ZERO = new HashMap<String, Integer>() {{
        put("〇", 0);
        put("0", 0);
        put("Ο", 0);
        put("○", 0);
        put("Ｏ", 0);
        putAll(CC_NUMBERS);
    }};
    private static final Map<String, Integer> EN_MONTHS = new HashMap<String, Integer>() {{
        put("jan", 1);
        put("feb", 2);
        put("mar", 3);
        put("apr", 4);
        put("may", 5);
        put("jun", 6);
        put("jul", 7);
        put("aug", 8);
        put("sep", 9);
        put("oct", 10);
        put("nov", 11);
        put("dec", 12);
    }};
    private static final String MONTHS_PATTERN = String.join("|", EN_MONTHS.keySet());
    private static final String WEEK_PATTERN = "Mon|Tue|Wed|Thu|Fri|Sat|Sun";
    private static final String CC_NUMBERS_WITH_ZERO_PATTERN = String.join("", CC_NUMBERS_WITH_ZERO.keySet());
    private static final String CC_NUMBERS_PATTERN = String.join("", CC_NUMBERS.keySet());
    // language=regexp
    public static final Pattern GENERIC_DATE_PATTERN = Pattern.compile("((?<y>\\d{4})[-/.年])?(?<m>\\d{1,2})[-/.月](?<d>\\d{1,2})日?");
    public static final Pattern CC_DATE_PATTERN = Pattern.compile("((?<y>[" + CC_NUMBERS_WITH_ZERO_PATTERN + "]{4})年)?(?<m>[" + CC_NUMBERS_PATTERN + "]{1,2})月(?<d>[" + CC_NUMBERS_PATTERN + "]{1,3})日?");
    // language=regexp
    public static final Pattern EN_TIME_PATTERN = Pattern.compile("(?<h>\\d{1,2}):(?<m>\\d{1,2})(:(?<s>\\d{1,2})(\\.(?<n>\\d{1,9}))?)?");
    // language=regexp
    public static final Pattern ZH_TIME_PATTERN = Pattern.compile("((?<h>\\d{1,2})[时点])((?<m>\\d{1,2})分)((?<s>\\d{1,2})秒)?");
    // language=regexp
    public static final Pattern ISO_DATE_TIME_PATTERN = Pattern.compile("(?<date>\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}(:\\d{2}(\\.\\d{1,9})?)?) ?(?<zone>Z|GMT|UTC|UT|([+-](\\d{2}:\\d{2}(:\\d{2})?|\\d{1,6})))?", Pattern.CASE_INSENSITIVE);
    // language=regexp
    public static final Pattern RFC_1123_DATE_TIME_PATTERN = Pattern.compile("(" + WEEK_PATTERN + "),\\s+\\d{1,2}\\s+(" + MONTHS_PATTERN + ")\\s+\\d{4}\\s+\\d{1,2}:\\d{1,2}:\\d{1,2}\\s+GMT", Pattern.CASE_INSENSITIVE);
    // language=regexp
    public static final Pattern RFC_CST_DATE_TIME_PATTERN = Pattern.compile("(" + WEEK_PATTERN + ")\\s+(?<M>" + MONTHS_PATTERN + ")\\s+(?<d>\\d{1,2})\\s+(?<time>\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+CST\\s+(?<y>\\d{4})", Pattern.CASE_INSENSITIVE);
    // language=regexp
    public static final Pattern RFC_GMT_DATE_TIME_PATTERN = Pattern.compile("(" + WEEK_PATTERN + ")\\s+(?<M>" + MONTHS_PATTERN + ")\\s+(?<d>\\d{1,2})\\s+(?<y>\\d{4})\\s+(?<time>\\d{1,2}:\\d{1,2}:\\d{1,2})\\s+GMT(?<zone>Z|GMT|UTC|UT|([+-](\\d{2}:\\d{2}(:\\d{2})?|\\d{1,6})))", Pattern.CASE_INSENSITIVE);
    public static final Pattern TIME_CHAR_PATTERN = Pattern.compile("[HhmsS]");

    public static final DateTimeFormatter DATE_NUM_FORMAT = DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_TIME_NUM_FORMAT = DateTimeFormatter.ofPattern("uuuuMMddHHmmss").withResolverStyle(ResolverStyle.STRICT);
    public static final DateTimeFormatter DATE_TIME_MILLS_NUM_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("uuuuMMddHHmmss").appendValue(ChronoField.MILLI_OF_SECOND, 3)
            .toFormatter().withResolverStyle(ResolverStyle.STRICT);

    private final ZonedDateTime dateTime;

    /**
     * Constructs a new MostDateTime with temporal.
     *
     * @param dateTime dateTime
     */
    MostDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Returns a new MostDateTime with temporal.
     * Zoned inputs retain their instant in the target zone; local values are interpreted in that zone.
     *
     * @param temporal temporal
     * @param zoneId   target zone; zoned inputs retain their instant, local times use today in this zone
     * @return MostDateTime instance
     */
    @Contract("null, _ -> fail")
    public static @NotNull MostDateTime of(Temporal temporal, ZoneId zoneId) {
        if (temporal instanceof ZonedDateTime) {
            return new MostDateTime(((ZonedDateTime) temporal).withZoneSameInstant(zoneId));
        }
        if (temporal instanceof OffsetDateTime) {
            return new MostDateTime(((OffsetDateTime) temporal).atZoneSameInstant(zoneId));
        }
        if (temporal instanceof LocalDateTime) {
            return new MostDateTime(((LocalDateTime) temporal).atZone(zoneId));
        }
        if (temporal instanceof Instant) {
            return new MostDateTime(((Instant) temporal).atZone(zoneId));
        }
        if (temporal instanceof LocalDate) {
            return new MostDateTime(((LocalDate) temporal).atStartOfDay(zoneId));
        }
        if (temporal instanceof LocalTime) {
            return new MostDateTime(((LocalTime) temporal).atDate(LocalDate.now(zoneId)).atZone(zoneId));
        }
        if (temporal instanceof OffsetTime) {
            return new MostDateTime(((OffsetTime) temporal).atDate(LocalDate.now(((OffsetTime) temporal).getOffset())).atZoneSameInstant(zoneId));
        }
        throw new IllegalArgumentException("Unsupported temporal type: " + temporal.getClass());
    }

    /**
     * Returns a new MostDateTime with temporal.
     * Zoned inputs retain their zone; values without a zone use the system default.
     *
     * @param temporal temporal
     * @return MostDateTime instance
     */
    @Contract("null -> fail")
    public static @NotNull MostDateTime of(Temporal temporal) {
        if (temporal instanceof ZonedDateTime) {
            return new MostDateTime((ZonedDateTime) temporal);
        }
        if (temporal instanceof OffsetDateTime) {
            return new MostDateTime(((OffsetDateTime) temporal).toZonedDateTime());
        }
        if (temporal instanceof OffsetTime) {
            OffsetTime time = (OffsetTime) temporal;
            return new MostDateTime(time.atDate(LocalDate.now(time.getOffset())).toZonedDateTime());
        }
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
        return new MostDateTime(ValueUtils.toTemporal(ZonedDateTime.class, date, zoneId));
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
     * @see #toZonedDateTime(String)
     */
    public static @NotNull MostDateTime of(String datetime) {
        ZonedDateTime ldt = toZonedDateTime(datetime);
        return of(ldt);
    }

    /**
     * Returns a new MostDateTime with string datetime and specific pattern.
     * Parses the entire input strictly and retains any zone or offset in the pattern.
     *
     * @param datetime string datetime
     * @param pattern  datetime pattern
     * @return MostDateTime instance
     */
    public static @NotNull MostDateTime of(String datetime, String pattern) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern(pattern)
                .parseDefaulting(ChronoField.ERA, 1).toFormatter().withResolverStyle(ResolverStyle.STRICT);
        TemporalAccessor parsed = formatter.parse(datetime);
        ZoneId zone = parsed.query(TemporalQueries.zone());
        if (zone == null) {
            zone = ZoneId.systemDefault();
        }
        if (parsed.isSupported(ChronoField.INSTANT_SECONDS)) {
            return of(Instant.from(parsed), zone);
        }
        LocalDate date = parsed.query(TemporalQueries.localDate());
        LocalTime time = parsed.query(TemporalQueries.localTime());
        if (date != null) {
            return of(time == null ? date.atStartOfDay(zone) : LocalDateTime.of(date, time).atZone(zone));
        }
        if (time != null) {
            return of(time, zone);
        }
        throw new IllegalArgumentException("Pattern must provide a date or time: " + pattern);
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
     * @return a new Instant
     */
    public Instant toInstant() {
        return dateTime.toInstant();
    }

    /**
     * Convert to Date.
     *
     * @return a new Date
     */
    public Date toDate() {
        return new Date(toInstant().toEpochMilli());
    }

    /**
     * Returns the local date-time without its zone.
     * @return local date-time
     */
    public LocalDateTime toLocalDateTime() {
        return dateTime.toLocalDateTime();
    }

    /**
     * Returns the immutable date-time with its zone.
     * @return zoned date-time
     */
    public ZonedDateTime getZonedDateTime() {
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
     * Convert to timestamp.
     *
     * @return timestamp
     */
    public long toEpochMilli() {
        return toInstant().toEpochMilli();
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
     * Convert string to zoned datetime object.
     * <p>
     * formats：
     * <ul>
     *     <li>13 bit timestamp</li>
     *     <li>10 bit timestamp</li>
     *     <li>{@code yyyyMMddHHmmssSSS}</li>
     *     <li>{@code yyyyMMddHHmmss}</li>
     *     <li>{@code yyyyMMdd}</li>
     *     <li>{@code yyyy[-/年]MM[-/月]dd[日]}</li>
     *     <li>{@code yyyy年MM月dd日 HH[时点]mm分ss秒}</li>
     *     <li>{@code yyyy[-/]MM[-/]dd HH:mm:ss} with 1 to 9 fractional second digits</li>
     *     <li>CC_Date, e.g. {@code 二〇二六年六月二十六日}</li>
     *     <li>ISO, e.g. {@code 2019-09-26T03:45:36.656+0800}</li>
     *     <li>RFC_1123, e.g. {@code Wed, 04 Jan 2023 09:36:48 GMT}</li>
     *     <li>RFC-like, e.g. {@code Wed Jan 04 2023 17:36:48 GMT+0800}</li>
     *     <li>RFC-like, e.g. {@code Wed Jan 04 18:52:01 CST 2023}</li>
     * </ul>
     *
     * Extracts recognized date-time parts from text. Use {@link #parse(String)} to validate the complete input.
     * Missing dates use today and missing years use the current year.
     *
     * @param datetime string datetime
     * @return ZonedDateTime
     */
    public static ZonedDateTime toZonedDateTime(@NotNull String datetime) {
        return parseDateTime(datetime, true);
    }

    /**
     * Parses the complete input, rejecting unrecognized prefixes and suffixes.
     * Supports the same formats as {@link #of(String)}. Missing dates use today;
     * missing years use the current year in the system default zone.
     * @param datetime date-time input
     * @return parsed date-time, retaining an explicit input zone
     * @throws IllegalArgumentException if the input cannot be parsed completely
     * @throws DateTimeException if a recognized date-time or zone is invalid
     */
    public static @NotNull MostDateTime parse(@NotNull String datetime) {
        return of(parseDateTime(datetime, false));
    }

    private static ZonedDateTime parseDateTime(String datetime, boolean extract) {
        datetime = datetime.trim();
        boolean isDigit = StringUtils.isAsciiDigits(datetime);
        int len = datetime.length();
        if (isDigit) {
            if (len == 17) {
                return LocalDateTime.parse(datetime, DATE_TIME_MILLS_NUM_FORMAT).atZone(ZoneId.systemDefault());
            }
            if (len == 14) {
                return LocalDateTime.parse(datetime, DATE_TIME_NUM_FORMAT).atZone(ZoneId.systemDefault());
            }
            if (len == 8) {
                return LocalDate.parse(datetime, DATE_NUM_FORMAT).atStartOfDay(ZoneId.systemDefault());
            }
            if (len == 13) {
                return Instant.ofEpochMilli(Long.parseLong(datetime)).atZone(ZoneId.systemDefault());
            }
            if (len == 10) {
                return Instant.ofEpochSecond(Long.parseLong(datetime)).atZone(ZoneId.systemDefault());
            }
        }

        if (RFC_1123_DATE_TIME_PATTERN.matcher(datetime).matches()) {
            return ZonedDateTime.parse(datetime, DateTimeFormatter.RFC_1123_DATE_TIME.withResolverStyle(ResolverStyle.STRICT));
        }

        // SQL-style timestamps use a space in place of ISO's date/time separator.
        String isoDatetime = len > 10 && datetime.charAt(10) == ' '
                ? datetime.substring(0, 10) + 'T' + datetime.substring(11) : datetime;
        if (isoDatetime.indexOf('T') >= 0 || isoDatetime.indexOf('t') >= 0) {
            try {
                Temporal parsed = (Temporal) DateTimeFormatter.ISO_DATE_TIME.parseBest(isoDatetime,
                        ZonedDateTime::from, LocalDateTime::from);
                return of(parsed).getZonedDateTime();
            } catch (DateTimeParseException ignored) {
                // Legacy inputs also allow compact offsets such as +0800.
            }
        }

        ISODateTime isoDateTime = new ISODateTime(datetime, extract);
        if (isoDateTime.find()) {
            return isoDateTime.toZonedDateTime();
        }

        RFCLikeDate rfcLikeDate = new RFCLikeDate(datetime, extract);
        if (rfcLikeDate.find()) {
            return rfcLikeDate.toZonedDateTime();
        }

        CCDate ccDate = new CCDate(datetime, extract);
        if (ccDate.find()) {
            return ccDate.toLocalDate().atStartOfDay(ZoneId.systemDefault());
        }

        boolean anyMatch = false;
        LocalDate today = LocalDate.now();
        int year = today.getYear(), month = today.getMonthValue(), day = today.getDayOfMonth();
        int hour = 0, minute = 0, second = 0, nanoSeconds = 0;
        StringBuilder remaining = new StringBuilder(datetime);
        Matcher dateMatcher = GENERIC_DATE_PATTERN.matcher(datetime);
        if (dateMatcher.find()) {
            clearMatch(remaining, dateMatcher);
            anyMatch = true;
            if (dateMatcher.group("y") != null) {
                year = Integer.parseInt(dateMatcher.group("y"));
            } else {
                log.warn("Year part not found, use now year of: {}", datetime);
            }
            month = Integer.parseInt(dateMatcher.group("m"));
            day = Integer.parseInt(dateMatcher.group("d"));
        } else {
            log.warn("Date part not found, use today as default: {}", datetime);
        }

        Matcher timeMatcher = EN_TIME_PATTERN.matcher(datetime);
        if (timeMatcher.find()) {
            clearMatch(remaining, timeMatcher);
            anyMatch = true;
            hour = Integer.parseInt(timeMatcher.group("h"));
            minute = Integer.parseInt(timeMatcher.group("m"));
            if (timeMatcher.group("s") != null) {
                second = Integer.parseInt(timeMatcher.group("s"));
            }
            if (timeMatcher.group("n") != null) {
                String n = timeMatcher.group("n");
                nanoSeconds = Integer.parseInt((n + "000000000").substring(0, 9));
            }
        } else {
            Matcher zhTimeMatcher = ZH_TIME_PATTERN.matcher(datetime);
            if (zhTimeMatcher.find()) {
                clearMatch(remaining, zhTimeMatcher);
                anyMatch = true;
                hour = Integer.parseInt(zhTimeMatcher.group("h"));
                minute = Integer.parseInt(zhTimeMatcher.group("m"));
                if (zhTimeMatcher.group("s") != null) {
                    second = Integer.parseInt(zhTimeMatcher.group("s"));
                }
            }
        }
        if (anyMatch && (extract || StringUtils.isBlank(remaining.toString()))) {
            return LocalDateTime.of(year, month, day, hour, minute, second, nanoSeconds).atZone(ZoneId.systemDefault());
        }
        throw new IllegalArgumentException("unknown date time format: " + datetime);
    }

    private static void clearMatch(StringBuilder remaining, Matcher matcher) {
        for (int i = matcher.start(); i < matcher.end(); i++) {
            remaining.setCharAt(i, ' ');
        }
    }

    /**
     * Create ISO datetime object.
     *
     * @param datetime e.g. {@code 2019-09-26T03:45:36.656+0800}
     * @return ISO datetime object
     */
    public static ISODateTime createISODateTime(String datetime) {
        return new ISODateTime(datetime);
    }

    /**
     * Create RFC-like datetime object.
     *
     * @param datetime e.g. {@code Wed Jan 04 2023 17:36:48 GMT+0800}
     * @return RFC-like datetime object
     */
    public static RFCLikeDate createRFCLikeDateTime(String datetime) {
        return new RFCLikeDate(datetime);
    }

    /**
     * Create Chinese capital letters date object.
     *
     * @param date e.g. {@code 二〇二六年六月二十六日}
     * @return Chinese capital letters date object
     */
    public static CCDate createCCDate(String date) {
        return new CCDate(date);
    }

    /**
     * UTC datetime.
     */
    public static class ISODateTime {
        private boolean find = false;
        private String date;
        private ZoneId zoneId;

        public ISODateTime(String stringDate) {
            this(stringDate, true);
        }

        private ISODateTime(String stringDate, boolean extract) {
            Matcher m = ISO_DATE_TIME_PATTERN.matcher(stringDate.trim());
            if (extract ? m.find() : m.matches()) {
                find = true;
                date = m.group("date").replace(' ', 'T');
                String zone = m.group("zone");
                if (zone == null) {
                    zoneId = ZoneId.systemDefault();
                    return;
                }
                zoneId = ZoneId.of(zone.toUpperCase(Locale.ROOT));
            }
        }

        public ZonedDateTime toZonedDateTime() {
            return LocalDateTime.parse(date).atZone(zoneId);
        }

        public String getDate() {
            return date;
        }

        public ZoneId getZoneId() {
            return zoneId;
        }

        public boolean find() {
            return find;
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
        private boolean find = false;
        private Integer year;
        private Integer month;
        private Integer day;
        private String time;
        private ZoneId zoneId;

        public RFCLikeDate(String stringDate) {
            this(stringDate, true);
        }

        private RFCLikeDate(String stringDate, boolean extract) {
            stringDate = stringDate.trim();
            Matcher rfcCSTm = RFC_CST_DATE_TIME_PATTERN.matcher(stringDate);
            if (extract ? rfcCSTm.find() : rfcCSTm.matches()) {
                find = true;
                year = Integer.parseInt(rfcCSTm.group("y"));
                month = EN_MONTHS.get(rfcCSTm.group("M").toLowerCase(Locale.ROOT));
                day = Integer.parseInt(rfcCSTm.group("d"));
                time = rfcCSTm.group("time");
                // CST is ambiguous; retain the legacy system-zone interpretation.
                zoneId = ZoneId.systemDefault();
                return;
            }
            Matcher rfcGMTm = RFC_GMT_DATE_TIME_PATTERN.matcher(stringDate);
            if (extract ? rfcGMTm.find() : rfcGMTm.matches()) {
                find = true;
                year = Integer.parseInt(rfcGMTm.group("y"));
                month = EN_MONTHS.get(rfcGMTm.group("M").toLowerCase(Locale.ROOT));
                day = Integer.parseInt(rfcGMTm.group("d"));
                time = rfcGMTm.group("time");
                zoneId = ZoneId.of(rfcGMTm.group("zone").toUpperCase(Locale.ROOT));
            }
        }

        public ZonedDateTime toZonedDateTime() {
            LocalDate localDate = LocalDate.of(year, month, day);
            LocalTime localTime = LocalTime.parse(time);
            return LocalDateTime.of(localDate, localTime).atZone(zoneId);
        }

        public boolean find() {
            return find;
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
     * Chinese capital letters date.
     */
    public static class CCDate {
        private boolean find = false;
        private String year;
        private String month;
        private String day;

        public CCDate(String stringDate) {
            this(stringDate, true);
        }

        private CCDate(String stringDate, boolean extract) {
            stringDate = stringDate.trim();
            Matcher m = CC_DATE_PATTERN.matcher(stringDate);
            if (extract ? m.find() : m.matches()) {
                find = true;
                year = m.group("y");
                month = m.group("m");
                day = m.group("d");
            }
        }

        private Integer getNumber(char c) {
            String cs = String.valueOf(c);
            return CC_NUMBERS_WITH_ZERO.get(cs);
        }

        public boolean find() {
            return find;
        }

        public LocalDate toLocalDate() {
            int y;
            int m;
            int d;
            if (year != null) {
                String ys = getNumber(year.charAt(0)).toString() +
                        getNumber(year.charAt(1)).toString() +
                        getNumber(year.charAt(2)).toString() +
                        getNumber(year.charAt(3)).toString();
                y = Integer.parseInt(ys);
            } else {
                y = LocalDate.now().getYear();
                log.warn("Year not found, now year as the default: {}", y);
            }

            m = getNumber(month.charAt(0));
            if (month.length() == 2) {
                m += getNumber(month.charAt(1));
            }

            d = getNumber(day.charAt(0));
            if (day.length() > 1) {
                int n = getNumber(day.charAt(1));
                d = d == 10 ? d : d * 10;
                if (n != 10) {
                    d += n;
                }
            }
            if (day.length() == 3) {
                int n = getNumber(day.charAt(2));
                d += n;
            }
            return LocalDate.of(y, m, d);
        }

        public String getYear() {
            return year;
        }

        public String getMonth() {
            return month;
        }

        public String getDay() {
            return day;
        }

        @Override
        public String toString() {
            return "CCDate{" +
                    "day='" + day + '\'' +
                    ", month='" + month + '\'' +
                    ", year='" + year + '\'' +
                    '}';
        }
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
