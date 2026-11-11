---
id: log4j-1
title: 'Log through SLF4J, not Log4j 1.x'
category: libraries
kind: hazard
note: 'Log4j 1.x reached end of life in August 2015 and has known unpatched vulnerabilities. Code against the SLF4J API and let Logback or Log4j 2 do the work, as Spring Boot does by default.'
source: https://logging.apache.org/log4j/1.x/
modern-since: 2012
limit: 1
detect:
  - 'code *.java /^\s*import\s+org\.apache\.log4j\./'
  - 'raw pom.xml,build.gradle,build.gradle.kts /<groupId>log4j<\/groupId>|[''"]log4j:log4j:/'
---

## Before

```java
import org.apache.log4j.Logger;

public class OrderService {
    private static final Logger log = Logger.getLogger(OrderService.class);

    void ship(Order order) {
        log.info("Shipping order " + order.id());
    }
}
```

```xml
<dependency>
    <groupId>log4j</groupId>
    <artifactId>log4j</artifactId>
    <version>1.2.17</version>
</dependency>
```

## After

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    void ship(Order order) {
        log.info("Shipping order {}", order.id());
    }
}
```

## Notes

- Use `{}` placeholders. The message is only built if the level is enabled.
- In a Spring Boot application the logging starter is already present. Remove the Log4j dependency and add nothing.
- The package `org.apache.logging.log4j` is Log4j 2, which is maintained. This guide is about `org.apache.log4j`.
