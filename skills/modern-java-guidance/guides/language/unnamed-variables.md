---
id: unnamed-variables
title: 'Use _ for variables you never read'
category: language
kind: superseded
applies: java>=22
note: 'Unnamed variables and patterns (Java 22) let you write catch (NumberFormatException _), so nobody has to wonder whether a variable called "ignored" is used.'
source: https://openjdk.org/jeps/456
modern-since: 2024
detect:
  - 'code *.java /\bcatch\s*\(\s*[\w.|\s]+\s+(?:ignored?|unused|expected)\s*\)/'
---

## Before

```java
try {
    return Integer.parseInt(text);
} catch (NumberFormatException ignored) {
    return fallback;
}
```

## After

```java
try {
    return Integer.parseInt(text);
} catch (NumberFormatException _) {
    return fallback;
}
```

## Notes

- `_` works anywhere a variable must be declared but is not used: lambda parameters `(_, value) -> value`, enhanced `for` loops, try-with-resources, and record patterns `case Point(int x, _)`.
