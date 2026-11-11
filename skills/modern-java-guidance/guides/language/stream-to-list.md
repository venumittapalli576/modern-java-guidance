---
id: stream-to-list
title: 'Use Stream.toList() instead of collect(Collectors.toList())'
category: language
kind: superseded
applies: java>=16
note: 'Stream.toList() (Java 16) is shorter and returns an unmodifiable list. Keep collect(Collectors.toList()) only where the caller goes on to mutate the result.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html
modern-since: 2021
detect:
  - 'code *.java /\.collect\(\s*(?:Collectors\.)?to(?:Unmodifiable)?List\(\)\s*\)/'
---

## Before

```java
List<String> emails = customers.stream()
        .map(Customer::getEmail)
        .collect(Collectors.toList());
```

## After

```java
List<String> emails = customers.stream()
        .map(Customer::getEmail)
        .toList();
```

## Notes

- The result cannot be modified. If later code calls `add` or `sort` on it, collect into `new ArrayList<>(...)` or use `Collectors.toCollection(ArrayList::new)`.
- Unlike `List.of`, `toList()` allows null elements.
