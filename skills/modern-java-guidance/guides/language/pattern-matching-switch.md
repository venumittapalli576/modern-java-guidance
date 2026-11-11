---
id: pattern-matching-switch
title: 'Use switch with type patterns instead of instanceof chains'
category: language
kind: superseded
applies: java>=21
note: 'Pattern matching for switch (Java 21) replaces if/else-if chains of instanceof. Over a sealed interface the compiler verifies that every subtype is covered.'
source: https://openjdk.org/jeps/441
modern-since: 2023
limit: 1
detect:
  - 'code *.java /^\s*\}?\s*else\s+if\s*\(\s*\w+\s+instanceof\s+[\w.<>?, ]+\)\s*\{?\s*$/'
---

## Before

```java
static double area(Shape shape) {
    if (shape instanceof Circle c) {
        return Math.PI * c.radius() * c.radius();
    } else if (shape instanceof Rectangle r) {
        return r.width() * r.height();
    } else {
        throw new IllegalArgumentException("Unknown shape: " + shape);
    }
}
```

## After

```java
sealed interface Shape permits Circle, Rectangle {}
record Circle(double radius) implements Shape {}
record Rectangle(double width, double height) implements Shape {}

static double area(Shape shape) {
    return switch (shape) {
        case Circle(double radius) -> Math.PI * radius * radius;
        case Rectangle(double width, double height) -> width * height;
    };
}
```

## Notes

- Sealing the hierarchy is what removes the `else throw`. Add a new `Shape` and every switch that misses it stops compiling.
- Guards go after `when`: `case Circle c when c.radius() == 0 -> 0;`
- `case null ->` handles null explicitly. Without it a null selector throws `NullPointerException`, as before.
