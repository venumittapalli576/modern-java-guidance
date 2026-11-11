---
id: relocated-artifacts
title: 'Use the current coordinates for relocated libraries'
category: build
kind: deprecated
applies: boot>=3.0
note: 'These libraries moved: mysql-connector-java to com.mysql:mysql-connector-j, org.hibernate to org.hibernate.orm, javax API jars to jakarta ones. Spring Boot 3 no longer manages the old coordinates.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
modern-since: 2022
detect:
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /mysql-connector-java|javax\.servlet-api|javax\.persistence-api|javax\.validation:validation-api|<artifactId>validation-api<\/artifactId>|thymeleaf-extras-springsecurity[45]|hibernate-entitymanager|[''"]org\.hibernate:hibernate-core|<groupId>org\.hibernate<\/groupId>\s*<artifactId>hibernate-core</m'
---

A dependency block copied from an older project resolves to a years-old version, or fails with "version is missing" because the Boot BOM no longer knows the artifact.

## Before

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity5</artifactId>
</dependency>
```

## After

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

## Notes

| Old coordinates | Current coordinates |
|---|---|
| `mysql:mysql-connector-java` | `com.mysql:mysql-connector-j` |
| `org.hibernate:hibernate-core` | `org.hibernate.orm:hibernate-core` |
| `org.hibernate:hibernate-entitymanager` | Merged into `hibernate-core` |
| `javax.servlet:javax.servlet-api` | `jakarta.servlet:jakarta.servlet-api` |
| `javax.persistence:javax.persistence-api` | `jakarta.persistence:jakarta.persistence-api` |
| `javax.validation:validation-api` | `jakarta.validation:jakarta.validation-api` |
| `thymeleaf-extras-springsecurity5` | `thymeleaf-extras-springsecurity6` |

- With Spring Boot starters you seldom need to name Hibernate, the servlet API or the validation API directly.
