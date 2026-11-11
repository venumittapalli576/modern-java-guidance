---
id: virtual-threads
title: 'Use virtual threads for blocking I/O instead of hand-sized thread pools'
category: concurrency
kind: superseded
applies: java>=21
note: 'Virtual threads (Java 21) make one thread per task cheap. Use Executors.newVirtualThreadPerTaskExecutor() for blocking I/O, and keep bounded platform pools for CPU-bound work.'
source: https://openjdk.org/jeps/444
modern-since: 2023
limit: 1
detect:
  - 'code *.java /\bExecutors\.new(?:Fixed|Cached)ThreadPool\s*\(/'
---

A fixed pool of 20 threads means 20 concurrent blocking calls and a queue behind them. With virtual threads the limit is the downstream system, not the pool.

## Before

```java
ExecutorService executor = Executors.newFixedThreadPool(20);
for (Order order : orders) {
    executor.submit(() -> shippingClient.schedule(order));
}
executor.shutdown();
```

## After

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (Order order : orders) {
        executor.submit(() -> shippingClient.schedule(order));
    }
}
```

## Notes

- In Spring Boot 3.2 or later, `spring.threads.virtual.enabled=true` puts request handling, `@Async` and listener containers on virtual threads.
- Never pool virtual threads. To cap concurrent calls to a fragile dependency, use a `Semaphore`.
- CPU-bound work gains nothing from virtual threads. Keep a platform pool sized to the cores for that.
- An `ExecutorService` is `AutoCloseable` since Java 19. Closing it waits for the submitted tasks.
- For a single background task, `Thread.ofVirtual().name("cache-warm-up").start(task)` replaces `new Thread(task).start()`. Shutdown hooks and long-lived service threads stay platform threads.
