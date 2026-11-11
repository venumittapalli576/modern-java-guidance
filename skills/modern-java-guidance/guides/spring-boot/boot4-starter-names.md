---
id: boot4-starter-names
title: 'Use the Spring Boot 4 starter names'
category: spring-boot
kind: deprecated
applies: boot>=4.0
note: 'Spring Boot 4 renamed several starters, for example spring-boot-starter-web to spring-boot-starter-webmvc. The old names still resolve but are deprecated and will be removed.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
modern-since: 2025
detect:
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /spring-boot-starter-(?:web|aop|web-services|oauth2-client|oauth2-resource-server|oauth2-authorization-server)(?![\w-])/'
---

Boot 4 is modular: each technology has its own module, starter and test starter. The renamed starters are the visible part of that.

## Before

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

## After

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aspectj</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-oauth2-resource-server</artifactId>
</dependency>
```

## Notes

| Spring Boot 3 | Spring Boot 4 |
|---|---|
| `spring-boot-starter-web` | `spring-boot-starter-webmvc` |
| `spring-boot-starter-aop` | `spring-boot-starter-aspectj` |
| `spring-boot-starter-web-services` | `spring-boot-starter-webservices` |
| `spring-boot-starter-oauth2-client` | `spring-boot-starter-security-oauth2-client` |
| `spring-boot-starter-oauth2-resource-server` | `spring-boot-starter-security-oauth2-resource-server` |
| `spring-boot-starter-oauth2-authorization-server` | `spring-boot-starter-security-oauth2-authorization-server` |

- Test support follows the same pattern: `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-starter-security-test`.
- Flyway and Liquibase now need `spring-boot-starter-flyway` or `spring-boot-starter-liquibase`, not just the third-party dependency.
- `RestClient` needs `spring-boot-starter-restclient`, and `WebClient` needs `spring-boot-starter-webclient`.
- Check whether you need the AspectJ starter at all. Many applications added `starter-aop` out of habit.
