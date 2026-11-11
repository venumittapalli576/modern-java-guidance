---
id: boot4-test-slice-imports
title: 'Import test slice annotations from their Spring Boot 4 modules'
category: spring-testing
kind: removed
applies: boot>=4.0
note: 'Spring Boot 4 split test auto-configuration into per-technology modules. @WebMvcTest, @AutoConfigureMockMvc, @DataJpaTest and the other slices have new packages and need their own test starter.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
modern-since: 2025
detect:
  - 'code *.java /^\s*import\s+org\.springframework\.boot\.test\.autoconfigure\.(?:web\.servlet|web\.reactive|web\.client|orm\.jpa|jdbc|data\.\w+)\./'
---

Every Boot 3 example of a slice test has an import that does not compile on Boot 4. The annotations are the same. Only the package and the dependency changed.

## Before

```java
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
```

## After

```java
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Notes

- The pattern is `org.springframework.boot.<module>.test.autoconfigure`, with a matching `spring-boot-starter-<technology>-test` starter.
- `@SpringBootTest` no longer sets up `MockMvc` on its own. Add `@AutoConfigureMockMvc`.
- `spring-boot-starter-test-classic` restores the Boot 3 behavior as a temporary bridge while you migrate.
