---
id: jakarta-ee-namespace
title: 'Import Jakarta EE APIs from jakarta.*, not javax.*'
category: jakarta
kind: removed
applies: boot>=3.0
note: 'Spring Boot 3 moved to Jakarta EE 9+. The javax.persistence, javax.servlet, javax.validation and related packages are not on the classpath, so these imports do not compile.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
modern-since: 2022
limit: 1
detect:
  - 'code *.java /^\s*import\s+(?:static\s+)?javax\.(?:persistence|servlet|validation|inject|ws\.rs|jms|mail|json|websocket|ejb|enterprise|faces|el|interceptor|decorator|resource|batch|jws|activation|xml\.(?:bind(?!\.DatatypeConverter\b)|ws|soap)|transaction\.(?!xa\b)|security\.(?:enterprise|jacc|auth\.message)|annotation\.(?:PostConstruct|PreDestroy|Resource|Resources|Priority|Generated|ManagedBean|security|sql))\b/'
---

Java EE became Jakarta EE and its packages were renamed from `javax.*` to `jakarta.*`. Spring Framework 6 and Spring Boot 3 only support the new namespace. Most Java code on the internet predates the change, so generated code keeps reaching for `javax`.

## Before

```java
import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.constraints.NotBlank;

@Entity
public class Customer {
    @Id
    private Long id;

    @NotBlank
    private String name;
}
```

## After

```java
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Customer {
    @Id
    private Long id;

    @NotBlank
    private String name;
}
```

## Notes

- Only the Java EE packages moved. These stay `javax` because they belong to the JDK or to other specifications: `javax.sql`, `javax.crypto`, `javax.net.ssl`, `javax.naming`, `javax.management`, `javax.xml.parsers`, `javax.xml.transform`, `javax.annotation.processing`, `javax.transaction.xa`, `javax.cache`.
- Dependencies change too: `jakarta.persistence:jakarta.persistence-api`, `jakarta.validation:jakarta.validation-api`, `jakarta.servlet:jakarta.servlet-api`. With Spring Boot starters you rarely declare these yourself.
- For a whole codebase, run the OpenRewrite recipe `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta` instead of editing imports by hand.
