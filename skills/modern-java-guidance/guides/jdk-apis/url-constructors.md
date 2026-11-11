---
id: url-constructors
title: 'Create URLs with URI.create(...).toURL() instead of new URL(...)'
category: jdk-apis
kind: deprecated
applies: java>=20
note: 'The java.net.URL constructors have been deprecated since Java 20. They neither validate nor encode. Parse with URI, then convert.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URL.html
modern-since: 2023
detect:
  - 'code *.java /\bnew\s+(?:java\.net\.)?URL\s*\(/'
---

## Before

```java
URL endpoint = new URL("https://api.example.com/v1/orders");
```

## After

```java
URL endpoint = URI.create("https://api.example.com/v1/orders").toURL();
```

## Notes

- Most APIs accept a `URI`, so you often do not need the `URL` at all. `java.net.http.HttpRequest` takes a `URI`.
- `URL.equals` and `hashCode` resolve host names over the network. Never use `URL` as a map key. Use `URI`.
