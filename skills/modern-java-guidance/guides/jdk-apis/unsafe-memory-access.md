---
id: unsafe-memory-access
title: 'Use VarHandle or the Foreign Function and Memory API instead of sun.misc.Unsafe'
category: jdk-apis
kind: deprecated
applies: java>=23
note: 'The memory-access methods of sun.misc.Unsafe were deprecated for removal in Java 23 (JEP 471) and warn at run time since Java 24 (JEP 498). VarHandle and the FFM API replace them.'
source: https://openjdk.org/jeps/471
modern-since: 2024
detect:
  - 'code *.java /\bsun\.misc\.Unsafe\b/'
---

## Before

```java
private static final sun.misc.Unsafe UNSAFE = loadUnsafe();
private static final long COUNT_OFFSET = UNSAFE.objectFieldOffset(Counter.class.getDeclaredField("count"));

boolean increment(int expected) {
    return UNSAFE.compareAndSwapInt(this, COUNT_OFFSET, expected, expected + 1);
}
```

## After

```java
private static final VarHandle COUNT;

static {
    try {
        COUNT = MethodHandles.lookup().findVarHandle(Counter.class, "count", int.class);
    } catch (ReflectiveOperationException e) {
        throw new ExceptionInInitializerError(e);
    }
}

boolean increment(int expected) {
    return COUNT.compareAndSet(this, expected, expected + 1);
}
```

## Notes

- On-heap field and array access: `VarHandle` (Java 9). For a plain counter, `AtomicInteger` or `LongAdder` is simpler still.
- Off-heap memory: `Arena` and `MemorySegment` from the Foreign Function and Memory API (final in Java 22).
