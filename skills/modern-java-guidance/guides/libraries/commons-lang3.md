---
id: commons-lang3
title: 'Use commons-lang3 and commons-collections4, not the old lines'
category: libraries
kind: deprecated
note: 'org.apache.commons.lang (2.x, last released in 2011) and org.apache.commons.collections (3.x) are frozen. Their successors live in new packages, lang3 and collections4.'
source: https://commons.apache.org/proper/commons-lang/
modern-since: 2011
detect:
  - 'code *.java /^\s*import\s+(?:static\s+)?org\.apache\.commons\.(?:lang|collections)\./'
---

## Before

```java
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
```

## After

```java
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
```

## Notes

- Coordinates: `org.apache.commons:commons-lang3` and `org.apache.commons:commons-collections4`.
- Check whether you still need the library at all. `String.isBlank`, `String.join`, `Objects.requireNonNullElse` and `List.copyOf` cover many classic `StringUtils` and `CollectionUtils` calls.
