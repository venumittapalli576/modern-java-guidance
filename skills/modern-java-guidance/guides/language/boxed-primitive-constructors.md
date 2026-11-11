---
id: boxed-primitive-constructors
title: 'Use valueOf or autoboxing instead of new Integer(...) and other wrapper constructors'
category: language
kind: deprecated
applies: java>=9
note: 'The wrapper class constructors have been deprecated since Java 9 and marked for removal since Java 16. They allocate on every call, where valueOf reuses cached instances.'
source: https://openjdk.org/jeps/390
modern-since: 2004
detect:
  - 'code *.java /\bnew\s+(?:Integer|Long|Double|Float|Short|Byte|Character|Boolean)\s*\(/'
---

## Before

```java
Integer count = new Integer(42);
Long id = new Long(rawId);
Boolean active = new Boolean("true");
```

## After

```java
Integer count = 42;
Long id = Long.valueOf(rawId);
boolean active = Boolean.parseBoolean("true");
```

## Notes

- To parse text, use `Integer.parseInt`, `Long.parseLong` and `Boolean.parseBoolean`, which return primitives.
