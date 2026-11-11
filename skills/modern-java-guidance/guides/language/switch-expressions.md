---
id: switch-expressions
title: 'Use arrow-form switch instead of case labels with break'
category: language
kind: superseded
applies: java>=14
note: 'Switch expressions (Java 14) use case X -> with no fall-through, return a value, and make the compiler check that every enum constant is handled.'
source: https://openjdk.org/jeps/361
modern-since: 2020
limit: 1
detect:
  - 'code *.java /^\s*case\s+(?:(?!->)[^:\n])+:(?!:)/'
---

## Before

```java
String label;
switch (status) {
    case PENDING:
    case PROCESSING:
        label = "In progress";
        break;
    case SHIPPED:
        label = "On its way";
        break;
    default:
        label = "Unknown";
}
```

## After

```java
String label = switch (status) {
    case PENDING, PROCESSING -> "In progress";
    case SHIPPED -> "On its way";
    case DELIVERED, CANCELLED -> "Closed";
};
```

## Notes

- Leave out `default` when switching over an enum or sealed type. The compiler then reports any constant you forgot, including ones added later.
- A branch with several statements uses a block and `yield`: `case SHIPPED -> { audit(order); yield "On its way"; }`
- Colon-form labels are still legal, and still right for the rare switch that needs fall-through.
