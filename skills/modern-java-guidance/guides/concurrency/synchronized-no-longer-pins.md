---
id: synchronized-no-longer-pins
title: 'Do not replace synchronized with ReentrantLock just for virtual threads'
category: concurrency
kind: hazard
applies: java>=24
note: 'Since Java 24 (JEP 491) a virtual thread that blocks inside synchronized releases its carrier thread. Rewriting synchronized to ReentrantLock to avoid pinning is advice from the Java 21 era.'
source: https://openjdk.org/jeps/491
modern-since: 2025
detect:
  - 'comments *.java /(?:avoid|avoids|avoiding|prevent|prevents|preventing|cause|causes)\s+(?:virtual[- ]thread\s+|carrier[- ]thread\s+|thread\s+)?pinning|pin(?:s|ning)?\s+(?:the\s+)?(?:carrier|virtual)\b/i'
---

On Java 21 to 23 a virtual thread that blocked while holding a monitor stayed pinned to its carrier, and "replace `synchronized` with `ReentrantLock`" became standard advice. JDK 24 removed the limitation. The rewrite now adds risk, such as a missed `unlock()`, for no benefit.

## Before

```java
// ReentrantLock instead of synchronized to avoid pinning the carrier thread
private final ReentrantLock lock = new ReentrantLock();

void refresh() {
    lock.lock();
    try {
        cache = loader.load();
    } finally {
        lock.unlock();
    }
}
```

## After

```java
synchronized void refresh() {
    cache = loader.load();
}
```

## Notes

- Choose between `synchronized` and `java.util.concurrent.locks` on what you need: timed or interruptible acquisition, fairness, or several conditions call for a `Lock`. Otherwise `synchronized` is simpler.
- If the project targets Java 21 to 23, the pinning concern is real and this guide does not apply.
- Pinning can still happen in native frames. `-Djdk.tracePinnedThreads` is gone. Use the `jdk.VirtualThreadPinned` JFR event to find it.
