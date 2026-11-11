---
id: string-templates-withdrawn
title: 'Do not use string templates (STR."..."), the feature was withdrawn'
category: language
kind: hazard
removed: java>=23
note: 'String templates were a preview in Java 21 and 22 and were withdrawn in Java 23. No release since has them, so STR."..." does not compile.'
source: https://openjdk.org/jeps/465
modern-since: 2020
detect:
  - 'code *.java /\b(?:STR|FMT|RAW)\s*\.\s*"/'
---

String templates appear in many "what's new in Java 21" articles, so they turn up in generated code as if they were a current feature. They never left preview and were pulled for redesign.

## Before

```java
String message = STR."Order \{order.id()} total: \{order.total()}";
```

## After

```java
String message = "Order %s total: %s".formatted(order.id(), order.total());
```

## Notes

- `String.formatted` (Java 15) and plain concatenation are the supported options. `MessageFormat` is there for localized text.
- There is no replacement proposal in any release through Java 27.
