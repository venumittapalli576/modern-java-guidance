---
id: structured-concurrency-preview
title: 'Treat StructuredTaskScope as a preview API'
category: concurrency
kind: hazard
note: 'Structured concurrency is still a preview in Java 27 (seventh preview, JEP 533). It needs --enable-preview, and its API changed in Java 25, so Java 21 code using ShutdownOnFailure no longer compiles.'
source: https://openjdk.org/jeps/533
modern-since: 2023
limit: 1
detect:
  - 'code *.java /\bStructuredTaskScope\b/'
---

`StructuredTaskScope` is shown in most virtual-thread articles as if it were finished. It is not. Preview APIs can change shape between releases, and class files compiled with `--enable-preview` only run on that exact Java version.

## Before

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Supplier<User> user = scope.fork(() -> users.find(id));
    Supplier<List<Order>> orders = scope.fork(() -> orderService.forUser(id));
    scope.join().throwIfFailed();
    return new Dashboard(user.get(), orders.get());
}
```

## After

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<User> user = executor.submit(() -> users.find(id));
    Future<List<Order>> orders = executor.submit(() -> orderService.forUser(id));
    return new Dashboard(user.get(), orders.get());
}
```

## Notes

- The After example uses only final APIs. It lacks automatic cancellation of the sibling task when one fails, which is exactly what structured concurrency will add.
- If you do opt in to the preview, write it for your exact JDK. Since Java 25 a scope is opened with `StructuredTaskScope.open(...)` and a `Joiner`, and the Java 21 subclasses are gone.
- Do not use preview features in a library or in anything that must run on more than one Java version.
