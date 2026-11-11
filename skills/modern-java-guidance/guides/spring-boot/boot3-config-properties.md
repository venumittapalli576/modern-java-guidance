---
id: boot3-config-properties
title: 'Replace configuration keys that Spring Boot 3 moved'
category: spring-boot
kind: removed
applies: boot>=3.0
note: 'Spring Boot 3 moved these keys, for example spring.redis.* to spring.data.redis.*. The old names no longer bind, so the application quietly falls back to defaults such as localhost.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
modern-since: 2022
detect:
  - 'code application*.properties,application*.yml,application*.yaml,bootstrap*.properties,bootstrap*.yml /^(?:spring\.redis\.|spring\.data\.cassandra\.|management\.metrics\.export\.|management\.trace\.http\.|spring\.zipkin\.|spring\.sleuth\.|spring\.jpa\.hibernate\.use-new-id-generator-mappings\b)/'
---

The dangerous part is that nothing fails. With `spring.redis.host` ignored, the application connects to `localhost:6379` and works on a developer machine.

## Before

```yaml
spring:
  redis:
    host: cache.internal
    port: 6380
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

## After

```yaml
spring:
  data:
    redis:
      host: cache.internal
      port: 6380
management:
  prometheus:
    metrics:
      export:
        enabled: true
```

## Notes

| Spring Boot 2 | Spring Boot 3 |
|---|---|
| `spring.redis.*` | `spring.data.redis.*` |
| `spring.data.cassandra.*` | `spring.cassandra.*` |
| `management.metrics.export.<product>.*` | `management.<product>.metrics.export.*` |
| `management.trace.http.*` | `management.httpexchanges.recording.*` |
| `spring.zipkin.*`, `spring.sleuth.*` | `management.zipkin.tracing.*`, `management.tracing.*` (Micrometer Tracing) |
| `spring.jpa.hibernate.use-new-id-generator-mappings` | Removed. Hibernate 6 always uses the new mappings |

- `spring-boot-properties-migrator` reports all of these at startup while you upgrade.
