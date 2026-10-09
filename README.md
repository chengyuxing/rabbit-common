# 一些常用公共的类和工具

- maven dependency (jdk1.8+)

  ```xml
  <dependency>
      <groupId>com.github.chengyuxing</groupId>
      <artifactId>rabbit-common</artifactId>
      <version>3.2.13</version>
  </dependency>
  ```

## MostDateTime

`MostDateTime.of(String)` 保留从文本中提取日期的能力，例如 `决定书二〇〇一年十二月二十一日的`。验证完整输入时使用 `MostDateTime.parse(String)`；非法日期、未识别的前后缀和多余的小数秒位数会报错，不会自动修正日期。`ValueUtils.adaptValue` 的日期及时间字符串转换使用这个完整校验入口。

```java
MostDateTime value = MostDateTime.parse("2026-10-09T12:34:56.123456789Z");
value.toLocalDateTime(); // 不包含时区
value.getZonedDateTime(); // 包含时区
value.toInstant();
```

支持 ISO/RFC、紧凑数字日期、中文日期和时间；小数秒支持 1～9 位。`of(datetime, pattern)` 按指定格式严格解析，保留格式中的时区和偏移，不将引号中的格式字母当成时间字段。

不传时区的 `of(Temporal)` 保留输入已有的时区；`of(Temporal, ZoneId)` 将同一时刻转换到目标时区，无时区的本地日期和时间则在目标时区解释。纯时间使用其所属时区的今天，缺少年份使用当前年份。RFC-like 字符串中的 `CST` 有歧义，为兼容既有行为仍使用系统默认时区；需要确定时区时请使用明确偏移或区域时区。

旧的无参 `toZonedDateTime()` 实际返回 `LocalDateTime`，已更名为 `toLocalDateTime()`，旧调用需要修改。需要带时区的结果时使用 `getZonedDateTime()`；静态的 `MostDateTime.toZonedDateTime(String)` 仍提供文本提取。

`java.util.Date` 字段转换会得到普通 `Date`，精度为毫秒；`Timestamp` 和 Java 时间类型保留支持的纳秒精度。JDBC 日期和时间可以安全转换，SQL 日期转 `LocalDate`、SQL 时间转 `LocalTime` 保留日历值，其余转换按 epoch 时间值和指定时区处理。
