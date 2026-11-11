---
id: boot4-undertow-removed
title: 'Move from Undertow to Tomcat or Jetty on Spring Boot 4'
category: spring-boot
kind: removed
applies: boot>=4.0
note: 'Spring Boot 4 requires Servlet 6.1, which Undertow does not implement. The Undertow starter and embedded server support were removed.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
modern-since: 2025
detect:
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /spring-boot-starter-undertow/'
---

Swapping Tomcat for Undertow was a popular tuning tip. On Boot 4 the dependency does not resolve.

## Before

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-undertow</artifactId>
</dependency>
```

## After

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

## Notes

- Tomcat is the default. For Jetty, exclude `spring-boot-starter-tomcat` and add `spring-boot-starter-jetty`.
- Remove any `server.undertow.*` properties.
- If the reason for Undertow was thread usage under blocking load, try `spring.threads.virtual.enabled=true` on Java 21 or later.
