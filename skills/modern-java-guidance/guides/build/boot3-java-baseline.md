---
id: boot3-java-baseline
title: 'Target Java 17 or later on Spring Boot 3 and 4'
category: build
kind: removed
applies: boot>=3.0
note: 'Spring Boot 3 and 4 need Java 17 as a minimum. A build that declares Java 8 or 11 cannot compile against, or run, the framework''s Java 17 class files.'
source: https://docs.spring.io/spring-boot/system-requirements.html
modern-since: 2022
detect:
  - 'code pom.xml /<java\.version>\s*(?:1\.[5-8]|[5-9]|1[0-6])\s*<|<maven\.compiler\.(?:release|source|target)>\s*(?:1\.[5-8]|[5-9]|1[0-6])\s*</'
  - 'strings build.gradle,build.gradle.kts /(?:source|target)Compatibility\s*=?\s*(?:JavaVersion\.VERSION_(?:1_[5-8]|9|1[0-6])\b|[''"](?:1\.[5-8]|9|1[0-6])[''"])|JavaLanguageVersion\.of\(\s*(?:[89]|1[0-6])\s*\)/'
---

`<java.version>1.8</java.version>` under a Spring Boot 3 parent is one of the most common generated build errors: a current Boot version pasted on top of a Java 8 era template.

## Before

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.7</version>
</parent>
<properties>
    <java.version>1.8</java.version>
</properties>
```

```groovy
plugins {
    id 'org.springframework.boot' version '3.5.7'
}
sourceCompatibility = '11'
```

## After

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.7</version>
</parent>
<properties>
    <java.version>21</java.version>
</properties>
```

## Notes

- Java 17 is the floor. Spring recommends the latest LTS, which is Java 25, and virtual threads need 21.
