---
id: standard-charsets
title: 'Use StandardCharsets.UTF_8 instead of the "UTF-8" string'
category: jdk-apis
kind: superseded
note: 'StandardCharsets (Java 7) has constants for the standard charsets, so getBytes, new String and URLEncoder need no name lookup and no UnsupportedEncodingException handling.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/charset/StandardCharsets.html
modern-since: 2011
detect:
  - 'strings *.java /(?:getBytes|new\s+String|Charset\.forName|URLEncoder\.encode|URLDecoder\.decode|InputStreamReader|OutputStreamWriter)\s*\([^;]*"(?i:utf-?8)"/'
---

## Before

```java
try {
    byte[] body = payload.getBytes("UTF-8");
    String query = URLEncoder.encode(term, "UTF-8");
} catch (UnsupportedEncodingException e) {
    throw new IllegalStateException(e);
}
```

## After

```java
byte[] body = payload.getBytes(StandardCharsets.UTF_8);
String query = URLEncoder.encode(term, StandardCharsets.UTF_8);
```

## Notes

- The `URLEncoder.encode(String, Charset)` and `URLDecoder.decode(String, Charset)` overloads need Java 10.
- Since Java 18 the default charset is UTF-8 everywhere (JEP 400), but stating it keeps the code correct on older runtimes too.
