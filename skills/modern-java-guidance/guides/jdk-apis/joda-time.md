---
id: joda-time
title: 'Use java.time instead of Joda-Time'
category: jdk-apis
kind: superseded
note: 'Joda-Time''s own site asks Java 8+ users to migrate to java.time (JSR-310), which the same author designed as its successor.'
source: https://www.joda.org/joda-time/
modern-since: 2014
limit: 1
detect:
  - 'code *.java /^\s*import\s+org\.joda\.time\./'
  - 'raw pom.xml,build.gradle,build.gradle.kts /\bjoda-time\b/'
---

## Before

```java
import org.joda.time.DateTime;
import org.joda.time.Days;

DateTime start = new DateTime(2026, 1, 15, 0, 0);
int days = Days.daysBetween(start, DateTime.now()).getDays();
```

```xml
<dependency>
    <groupId>joda-time</groupId>
    <artifactId>joda-time</artifactId>
</dependency>
```

## After

```java
LocalDate start = LocalDate.of(2026, 1, 15);
long days = ChronoUnit.DAYS.between(start, LocalDate.now(clock));
```

## Notes

- The mapping is close: `DateTime` to `ZonedDateTime` or `OffsetDateTime`, `LocalDate` to `LocalDate`, `Interval` has no direct equivalent (keep two `Instant`s), `DateTimeFormat` to `DateTimeFormatter`.
