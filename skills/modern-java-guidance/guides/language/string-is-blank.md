---
id: string-is-blank
title: 'Use isBlank() instead of trim().isEmpty()'
category: language
kind: superseded
applies: java>=11
note: 'String.isBlank() (Java 11) replaces trim().isEmpty() without allocating a new string, and strip() is the Unicode-aware successor of trim().'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html
modern-since: 2018
detect:
  - 'code *.java /\.trim\(\)\s*\.\s*(?:isEmpty\(\)|length\(\)\s*==\s*0)/'
---

## Before

```java
if (name == null || name.trim().isEmpty()) {
    throw new IllegalArgumentException("name is required");
}
```

## After

```java
if (name == null || name.isBlank()) {
    throw new IllegalArgumentException("name is required");
}
```

## Notes

- Java 11 also added `strip()`, `stripLeading()`, `stripTrailing()`, `repeat(n)` and `lines()`.
- `trim()` only removes characters up to U+0020. `strip()` removes all Unicode whitespace.
