---
id: guava-jdk-equivalents
title: 'Use the JDK where it has caught up with Guava'
category: libraries
kind: superseded
note: 'Guava''s own Javadoc calls Lists.newArrayList() unnecessary since Java 7. java.util.Optional, StandardCharsets and java.util.function likewise replace Guava''s Optional, Charsets and functional interfaces.'
source: https://guava.dev/releases/snapshot-jre/api/docs/com/google/common/collect/Lists.html
modern-since: 2014
detect:
  - 'code *.java /\b(?:Lists\.newArrayList|Maps\.newHashMap|Sets\.newHashSet)\s*\(\s*\)/'
  - 'code *.java /^\s*import\s+com\.google\.common\.base\.(?:Optional|Charsets|Function|Predicate|Supplier)\s*;/'
---

## Before

```java
import com.google.common.base.Optional;

List<String> names = Lists.newArrayList();
Map<String, Integer> counts = Maps.newHashMap();
Optional<User> user = Optional.fromNullable(found);
```

## After

```java
import java.util.Optional;

List<String> names = new ArrayList<>();
Map<String, Integer> counts = new HashMap<>();
Optional<User> user = Optional.ofNullable(found);
```

## Notes

- Plenty of Guava is still the best tool: `ImmutableList` and friends, `Multimap`, `Range`, `RateLimiter`, the cache. This guide is only about the pieces the JDK absorbed.
