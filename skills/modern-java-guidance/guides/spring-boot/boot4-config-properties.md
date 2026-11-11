---
id: boot4-config-properties
title: 'Replace configuration keys that Spring Boot 4 moved'
category: spring-boot
kind: removed
applies: boot>=4.0
note: 'Spring Boot 4 moved MongoDB connection settings to spring.mongodb.*, Jackson read and write features under spring.jackson.json.*, and Spring Session stores under spring.session.data.*.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
modern-since: 2025
detect:
  - 'code application*.properties,application*.yml,application*.yaml,bootstrap*.properties,bootstrap*.yml /^(?:spring\.session\.(?:redis|mongodb)\.|spring\.dao\.exceptiontranslation\.enabled\b|spring\.kafka\.retry\.topic\.backoff\.random\b|spring\.data\.mongodb\.(?:additional-hosts|authentication-database|database|host|password|port|protocol|replica-set-name|representation\.uuid|ssl\.[\w-]+|uri|username)\s*[:=]|spring\.jackson\.(?:read|write|parser)\.)/'
---

Boot 4's modules each own a configuration prefix, and several keys moved to match.

## Before

```properties
spring.data.mongodb.uri=mongodb://db.internal/shop
spring.jackson.write.indent-output=true
spring.session.redis.namespace=shop:session
```

## After

```properties
spring.mongodb.uri=mongodb://db.internal/shop
spring.jackson.json.write.indent-output=true
spring.session.data.redis.namespace=shop:session
```

## Notes

| Spring Boot 3 | Spring Boot 4 |
|---|---|
| `spring.data.mongodb.uri`, `.host`, `.port`, `.database`, `.username`, `.password`, `.ssl.*` | `spring.mongodb.*` |
| `spring.jackson.read.*`, `spring.jackson.write.*` | `spring.jackson.json.read.*`, `spring.jackson.json.write.*` |
| `spring.session.redis.*`, `spring.session.mongodb.*` | `spring.session.data.redis.*`, `spring.session.data.mongodb.*` |
| `spring.dao.exceptiontranslation.enabled` | `spring.persistence.exceptiontranslation.enabled` |
| `spring.kafka.retry.topic.backoff.random` | `spring.kafka.retry.topic.backoff.jitter` |

- Spring Data settings stay where they were: `spring.data.mongodb.auto-index-creation`, `spring.data.mongodb.repositories.type` and `spring.data.mongodb.gridfs.*`.
- Boot 4 has no default for MongoDB UUID and BigDecimal representation. Set `spring.mongodb.representation.uuid` and `spring.data.mongodb.representation.big-decimal` explicitly.
