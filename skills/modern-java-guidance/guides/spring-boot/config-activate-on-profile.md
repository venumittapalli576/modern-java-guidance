---
id: config-activate-on-profile
title: 'Use spring.config.activate.on-profile instead of spring.profiles in config documents'
category: spring-boot
kind: removed
applies: boot>=2.4
note: 'Since Spring Boot 2.4 a profile-specific document is marked with spring.config.activate.on-profile. A document that sets spring.profiles fails at startup with InvalidConfigDataPropertyException.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-Config-Data-Migration-Guide
modern-since: 2020
detect:
  - 'code application*.yml,application*.yaml,application*.properties,bootstrap*.yml,bootstrap*.yaml /^spring\.profiles\s*[:=]\s*\S/'
---

Multi-document `application.yml` files used `spring.profiles` to say which profile a document belongs to. Config data processing was rewritten in 2.4 and the key changed.

## Before

```yaml
server:
  port: 8080
---
spring:
  profiles: production
server:
  port: 80
```

## After

```yaml
server:
  port: 8080
---
spring:
  config:
    activate:
      on-profile: production
server:
  port: 80
```

## Notes

- `spring.profiles.active` and `spring.profiles.include` are unaffected, but neither can be set inside a document that is itself profile-specific.
- `spring.profiles.group.<name>` replaces most uses of `spring.profiles.include`.
