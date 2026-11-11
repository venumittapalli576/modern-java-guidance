---
id: scoped-values
title: 'Consider ScopedValue instead of ThreadLocal for request context'
category: concurrency
kind: superseded
applies: java>=25
note: 'Scoped values became final in Java 25 (JEP 506). They pass immutable context down a call chain, are unbound automatically, and cost little with virtual threads.'
source: https://openjdk.org/jeps/506
modern-since: 2025
limit: 1
detect:
  - 'code *.java /\bnew\s+(?:Inheritable)?ThreadLocal\s*<|\bThreadLocal\.withInitial\s*\(/'
---

A `ThreadLocal` stays set until someone remembers to call `remove()`, which is how tenant ids leak between requests on pooled threads. A scoped value is bound for the duration of one call and cannot outlive it.

## Before

```java
private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

void handle(Request request) {
    CONTEXT.set(RequestContext.from(request));
    try {
        process(request);
    } finally {
        CONTEXT.remove();
    }
}
```

## After

```java
private static final ScopedValue<RequestContext> CONTEXT = ScopedValue.newInstance();

void handle(Request request) {
    ScopedValue.where(CONTEXT, RequestContext.from(request)).run(() -> process(request));
}

String currentTenant() {
    return CONTEXT.get().tenant();
}
```

## Notes

- Scoped values are for one-way, read-only context such as the current user, tenant or trace id.
- `ThreadLocal` is still right for a mutable per-thread object, such as a cached buffer on a platform thread pool.
- Framework-managed context, for example Spring Security's `SecurityContextHolder` or SLF4J's MDC, stays as the framework provides it.
