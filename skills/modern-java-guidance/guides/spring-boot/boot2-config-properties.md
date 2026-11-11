---
id: boot2-config-properties
title: 'Replace configuration keys that Spring Boot 2 renamed or removed'
category: spring-boot
kind: removed
applies: boot>=2.7
note: 'These Spring Boot 1.x and early 2.x keys no longer bind, so the setting is silently ignored. server.context-path, management.security.enabled and logging.file are the usual offenders.'
source: https://docs.spring.io/spring-boot/appendix/application-properties/index.html
modern-since: 2018
detect:
  - 'code application*.properties,application*.yml,application*.yaml,bootstrap*.properties,bootstrap*.yml /^(?:server\.context-?[pP]ath|management\.security\.enabled|security\.basic\.enabled|endpoints\.[\w-]+\.(?:enabled|sensitive)|spring\.http\.multipart\.[\w-]+|spring\.datasource\.(?:initialization-mode|schema|data|platform)|logging\.(?:file|path))\s*[:=]\s*\S/'
---

Property names from Spring Boot 1.x tutorials still appear in generated configuration. Boot does not fail on an unknown key. It just does not apply it.

## Before

```properties
server.context-path=/shop
management.security.enabled=false
spring.http.multipart.max-file-size=10MB
spring.datasource.initialization-mode=always
logging.file=app.log
```

## After

```properties
server.servlet.context-path=/shop
management.endpoints.web.exposure.include=health,info,metrics
spring.servlet.multipart.max-file-size=10MB
spring.sql.init.mode=always
logging.file.name=app.log
```

## Notes

| Old key | Current key |
|---|---|
| `server.context-path` | `server.servlet.context-path` |
| `management.security.enabled`, `security.basic.enabled` | Removed. Secure or expose actuator endpoints with Spring Security and `management.endpoints.web.exposure.include` |
| `endpoints.<id>.enabled` | `management.endpoint.<id>.enabled` |
| `spring.http.multipart.*` | `spring.servlet.multipart.*` |
| `spring.datasource.initialization-mode`, `.schema`, `.data`, `.platform` | `spring.sql.init.mode`, `.schema-locations`, `.data-locations`, `.platform` |
| `logging.file`, `logging.path` | `logging.file.name`, `logging.file.path` |

- Adding `spring-boot-properties-migrator` as a runtime dependency during an upgrade prints every renamed key at startup. Remove it afterwards.
