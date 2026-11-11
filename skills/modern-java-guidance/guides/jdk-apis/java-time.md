---
id: java-time
title: 'Use java.time instead of Date, Calendar and SimpleDateFormat'
category: jdk-apis
kind: superseded
note: 'java.time (Java 8, 2014) replaces java.util.Date, Calendar and SimpleDateFormat. Its types are immutable and thread-safe. A shared SimpleDateFormat silently corrupts its output.'
source: https://openjdk.org/jeps/150
modern-since: 2014
limit: 3
detect:
  - 'code *.java /\bnew\s+(?:java\.util\.)?Date\s*\(/'
  - 'code *.java /\bCalendar\.getInstance\s*\(|\bnew\s+GregorianCalendar\s*\(/'
  - 'code *.java /\bnew\s+SimpleDateFormat\s*\(/'
---

## Before

```java
Date now = new Date();
Calendar calendar = Calendar.getInstance();
calendar.setTime(now);
calendar.add(Calendar.DAY_OF_MONTH, 30);
Date dueDate = calendar.getTime();
String formatted = new SimpleDateFormat("yyyy-MM-dd").format(dueDate);
```

## After

```java
LocalDate dueDate = LocalDate.now(clock).plusDays(30);
String formatted = dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
```

## Notes

| Need | Type |
|---|---|
| A point on the timeline, for timestamps and audit columns | `Instant` |
| A date with no time, for birthdays and due dates | `LocalDate` |
| Date and time in a named zone | `ZonedDateTime` |
| An amount of time | `Duration` (hours, seconds) or `Period` (days, months) |

- Inject a `Clock` and call `now(clock)` so tests can fix the time.
- JPA 2.2+, JDBC 4.2+ and Jackson all map `java.time` types directly.
- At a boundary with an API that still wants `Date`, convert at the edge: `Date.from(instant)` and `date.toInstant()`.
