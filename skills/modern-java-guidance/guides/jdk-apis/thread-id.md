---
id: thread-id
title: 'Use Thread.threadId() instead of Thread.getId()'
category: jdk-apis
kind: deprecated
applies: java>=19
note: 'Thread.getId() has been deprecated since Java 19 because it is not final, so a subclass can override it to return anything. threadId() is final.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Thread.html
modern-since: 2022
detect:
  - 'code *.java /\bThread\.currentThread\(\)\s*\.\s*getId\(\)/'
---

## Before

```java
log.debug("worker {} started", Thread.currentThread().getId());
```

## After

```java
log.debug("worker {} started", Thread.currentThread().threadId());
```

## Notes

- Do not key caches or per-thread state on thread ids once virtual threads are in play. There can be millions of them, and they are short-lived.
