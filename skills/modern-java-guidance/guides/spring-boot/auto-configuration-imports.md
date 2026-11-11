---
id: auto-configuration-imports
title: 'Register auto-configurations in AutoConfiguration.imports, not spring.factories'
category: spring-boot
kind: deprecated
applies: boot>=2.7
removed: boot>=3.0
note: 'Spring Boot 2.7 moved auto-configuration registration to META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports. Boot 3 ignores the old spring.factories key.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
modern-since: 2022
detect:
  - 'raw spring.factories /org\.springframework\.boot\.autoconfigure\.EnableAutoConfiguration\s*=/'
---

A starter that still lists its auto-configuration under `EnableAutoConfiguration` in `spring.factories` loads on Boot 2 and silently does nothing on Boot 3.

## Before

```properties file=META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.example.audit.AuditAutoConfiguration
```

## After

```text file=META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
com.example.audit.AuditAutoConfiguration
```

```java
@AutoConfiguration
@ConditionalOnClass(AuditClient.class)
public class AuditAutoConfiguration {
}
```

## Notes

- Annotate the class with `@AutoConfiguration` instead of `@Configuration`. Ordering moves to its `before` and `after` attributes.
- `spring.factories` is still used for other extension points, such as `ApplicationListener` and `EnvironmentPostProcessor`.
