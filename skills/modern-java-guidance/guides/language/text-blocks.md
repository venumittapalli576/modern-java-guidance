---
id: text-blocks
title: 'Use text blocks for multi-line strings'
category: language
kind: superseded
applies: java>=15
note: 'Text blocks (Java 15) hold SQL, JSON and other multi-line text between triple quotes, with no \n, no + and no escaped quotes.'
source: https://openjdk.org/jeps/378
modern-since: 2020
limit: 1
detect:
  - 'strings *.java /\\n"\s*\+\s*$/'
---

## Before

```java
String query = "SELECT o.id, o.total\n" +
               "FROM orders o\n" +
               "WHERE o.status = 'OPEN'\n" +
               "ORDER BY o.created_at DESC";
```

## After

```java
String query = """
        SELECT o.id, o.total
        FROM orders o
        WHERE o.status = 'OPEN'
        ORDER BY o.created_at DESC
        """;
```

## Notes

- Indentation common to all lines is stripped, so the block can be indented with the code around it.
- Fill in values with `formatted`: `"""{"id": %d}""".formatted(id)`. Never build SQL that way. Use bind parameters.
- End a line with `\` to join it to the next without a newline.
