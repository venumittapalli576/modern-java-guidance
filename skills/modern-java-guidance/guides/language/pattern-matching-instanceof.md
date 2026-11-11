---
id: pattern-matching-instanceof
title: 'Use pattern matching for instanceof instead of a cast on the next line'
category: language
kind: superseded
applies: java>=16
note: 'Since Java 16, instanceof can bind a variable: if (o instanceof Circle c). The test-then-cast pair is no longer needed and cannot get out of sync.'
source: https://openjdk.org/jeps/394
modern-since: 2021
detect:
  - 'code *.java /\binstanceof\s+([\w.]+)\s*\)\s*\{?\s*(?:final\s+)?\1(?:<[^>;]*>)?\s+\w+\s*=\s*\(\s*\1(?:<[^>;]*>)?\s*\)/m'
---

## Before

```java
if (shape instanceof Circle) {
    Circle circle = (Circle) shape;
    return Math.PI * circle.radius() * circle.radius();
}
```

## After

```java
if (shape instanceof Circle circle) {
    return Math.PI * circle.radius() * circle.radius();
}
```

## Notes

- The binding is in scope wherever the test is known to be true, including the rest of the condition: `if (o instanceof String s && !s.isBlank())`.
- It simplifies `equals`: `return o instanceof Point p && x == p.x && y == p.y;`
- With records you can destructure in the same step (Java 21): `if (shape instanceof Circle(double radius))`.
