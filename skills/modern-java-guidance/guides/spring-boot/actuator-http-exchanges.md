---
id: actuator-http-exchanges
title: 'Use HttpExchangeRepository instead of HttpTraceRepository'
category: spring-boot
kind: removed
applies: boot>=3.0
note: 'Spring Boot 3 renamed the httptrace actuator endpoint to httpexchanges. HttpTraceRepository and InMemoryHttpTraceRepository became HttpExchangeRepository and InMemoryHttpExchangeRepository.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
modern-since: 2022
detect:
  - 'code *.java /\b(?:InMemory)?HttpTraceRepository\b/'
---

## Before

```java
@Bean
HttpTraceRepository httpTraceRepository() {
    return new InMemoryHttpTraceRepository();
}
```

## After

```java
@Bean
HttpExchangeRepository httpExchangeRepository() {
    return new InMemoryHttpExchangeRepository();
}
```

## Notes

- Expose the endpoint under its new id: `management.endpoints.web.exposure.include=httpexchanges`.
- The in-memory repository is for development. In production, rely on tracing or access logs.
