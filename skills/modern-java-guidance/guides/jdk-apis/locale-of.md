---
id: locale-of
title: 'Use Locale.of(...) instead of new Locale(...)'
category: jdk-apis
kind: deprecated
applies: java>=19
note: 'The Locale constructors have been deprecated since Java 19 in favor of the Locale.of factory methods.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Locale.html
modern-since: 2022
detect:
  - 'code *.java /\bnew\s+(?:java\.util\.)?Locale\s*\(/'
---

## Before

```java
Locale locale = new Locale("en", "IN");
```

## After

```java
Locale locale = Locale.of("en", "IN");
```

## Notes

- For a language tag such as `"en-IN"`, use `Locale.forLanguageTag("en-IN")`.
- On Java 17 and earlier, `Locale.forLanguageTag` or `new Locale.Builder()` avoid the constructor too.
