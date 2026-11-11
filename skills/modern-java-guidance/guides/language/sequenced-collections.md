---
id: sequenced-collections
title: 'Use getFirst() and getLast() instead of index arithmetic'
category: language
kind: superseded
applies: java>=21
note: 'Sequenced collections (Java 21) give List, Deque and LinkedHashSet the methods getFirst(), getLast() and reversed(), replacing list.get(list.size() - 1).'
source: https://openjdk.org/jeps/431
modern-since: 2023
detect:
  - 'code *.java /\b([\w.]+)\.get\(\s*\1\.size\(\)\s*-\s*1\s*\)/'
---

## Before

```java
Event latest = events.get(events.size() - 1);
```

## After

```java
Event latest = events.getLast();
```

## Notes

- Both throw `NoSuchElementException` on an empty collection, where the index form threw `IndexOutOfBoundsException`.
- `reversed()` gives a reversed view without copying: `for (Event e : events.reversed())`.
- `LinkedHashMap` gained `firstEntry()`, `lastEntry()` and `sequencedKeySet()` at the same time.
