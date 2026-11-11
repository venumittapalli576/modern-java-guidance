---
id: jackson-java-time-module
title: 'Do not register JavaTimeModule by hand'
category: jackson
kind: superseded
note: 'Spring Boot''s mapper already handles java.time types, and in Jackson 3 (Spring Boot 4) that support is built into jackson-databind, so JavaTimeModule is not there to register.'
source: https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0
modern-since: 2014
detect:
  - 'code *.java /\bnew\s+JavaTimeModule\s*\(/'
  - 'code *.java /\.findAndRegisterModules\s*\(/'
---

`objectMapper.registerModule(new JavaTimeModule())` is muscle memory from standalone Jackson 2. In a Spring Boot application it was never needed for the auto-configured mapper, and with Jackson 3 the class is gone.

## Before

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.findAndRegisterModules();
```

## After

```java
JsonMapper mapper = JsonMapper.builder().build();
String json = mapper.writeValueAsString(LocalDate.of(2026, 11, 11));
```

## Notes

- Jackson 3 also folded the parameter-names and JDK 8 (`Optional`) modules into `jackson-databind`.
- In a Spring bean, do not build a mapper at all. Inject the one Boot configured.
