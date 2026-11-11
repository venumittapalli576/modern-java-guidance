---
id: finalizers
title: 'Use try-with-resources or Cleaner instead of finalize()'
category: language
kind: deprecated
applies: java>=9
note: 'Object.finalize() has been deprecated since Java 9 and marked for removal since Java 18 (JEP 421). Finalizers run late or never, and delay garbage collection.'
source: https://openjdk.org/jeps/421
modern-since: 2011
detect:
  - 'code *.java /\bvoid\s+finalize\s*\(\s*\)/'
---

## Before

```java
public class NativeBuffer {
    private final long address = allocate();

    @Override
    protected void finalize() throws Throwable {
        free(address);
    }
}
```

## After

```java
public class NativeBuffer implements AutoCloseable {
    private final long address = allocate();

    @Override
    public void close() {
        free(address);
    }
}
```

```java
try (NativeBuffer buffer = new NativeBuffer()) {
    buffer.write(payload);
}
```

## Notes

- Make the type `AutoCloseable` and let callers release it deterministically.
- If you need a safety net for callers who forget to close, register one with `java.lang.ref.Cleaner` (Java 9).
