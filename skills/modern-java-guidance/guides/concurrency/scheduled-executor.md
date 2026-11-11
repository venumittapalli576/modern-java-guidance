---
id: scheduled-executor
title: 'Use ScheduledExecutorService or @Scheduled instead of java.util.Timer'
category: concurrency
kind: superseded
note: 'ScheduledExecutorService (Java 5, 2004) replaced Timer. One TimerTask that throws kills the Timer thread and every other task on it, and Timer is sensitive to system clock changes.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Timer.html
modern-since: 2004
when-file: '\bjava\.util\.Timer(?:Task)?\b'
detect:
  - 'code *.java /\bnew\s+Timer\s*\(|\bextends\s+TimerTask\b|\bnew\s+TimerTask\s*\(/'
---

## Before

```java
import java.util.Timer;
import java.util.TimerTask;

Timer timer = new Timer();
timer.scheduleAtFixedRate(new TimerTask() {
    @Override
    public void run() {
        sessions.evictExpired();
    }
}, 0, 60_000);
```

## After

```java
ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
scheduler.scheduleAtFixedRate(sessions::evictExpired, 0, 1, TimeUnit.MINUTES);
```

## Notes

- In a Spring application, annotate the method instead: `@Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)` with `@EnableScheduling`.
- Catch exceptions inside the task. An uncaught exception cancels further runs of a scheduled task.
