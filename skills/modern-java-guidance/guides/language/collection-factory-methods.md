---
id: collection-factory-methods
title: 'Use List.of, Set.of and Map.of for small immutable collections'
category: language
kind: superseded
applies: java>=9
note: 'Since Java 9, List.of, Set.of and Map.of build an immutable collection in one call, replacing Collections.unmodifiableList(Arrays.asList(...)) and unmodifiableSet(new HashSet<>(Arrays.asList(...))).'
source: https://openjdk.org/jeps/269
modern-since: 2017
detect:
  - 'code *.java /\bCollections\.unmodifiable(?:List|Set|Collection)\s*\(\s*(?:new\s+[\w.]+(?:<[^>]*>)?\s*\(\s*)?Arrays\.asList\s*\(/'
---

## Before

```java
private static final List<String> ROLES =
        Collections.unmodifiableList(Arrays.asList("ADMIN", "USER", "AUDITOR"));

private static final Set<String> REGIONS =
        Collections.unmodifiableSet(new HashSet<>(Arrays.asList("us", "eu")));
```

## After

```java
private static final List<String> ROLES = List.of("ADMIN", "USER", "AUDITOR");

private static final Set<String> REGIONS = Set.of("us", "eu");
```

## Notes

- These collections reject `null` elements and throw on any mutation. `Set.of` and `Map.of` also reject duplicates.
- `List.copyOf(collection)` (Java 10) is the defensive copy for a constructor or getter.
- `Map.of` takes up to ten pairs. Beyond that use `Map.ofEntries(Map.entry(k, v), ...)`.
- `Set.of` and `Map.of` have no defined iteration order. A wrapper such as `Collections.unmodifiableMap(new LinkedHashMap<>(source))` that exists to keep insertion order should stay as it is.
