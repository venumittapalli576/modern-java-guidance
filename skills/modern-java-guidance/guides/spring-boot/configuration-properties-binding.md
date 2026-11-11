---
id: configuration-properties-binding
title: 'Bind @ConfigurationProperties to a record, without @ConstructorBinding'
category: spring-boot
kind: deprecated
applies: boot>=3.0
note: 'Since Spring Boot 3.0 a @ConfigurationProperties type with one constructor is bound through it automatically. @ConstructorBinding moved to the bind package and is only for choosing between constructors.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
modern-since: 2022
detect:
  - 'code *.java /^\s*import\s+org\.springframework\.boot\.context\.properties\.ConstructorBinding\s*;/'
---

Immutable configuration no longer needs an annotation to opt in. A record is the shortest way to write it.

## Before

```java
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties("shop.payments")
public class PaymentProperties {
    private final URI endpoint;
    private final Duration timeout;

    public PaymentProperties(URI endpoint, Duration timeout) {
        this.endpoint = endpoint;
        this.timeout = timeout;
    }
}
```

## After

```java
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("shop.payments")
public record PaymentProperties(URI endpoint, @DefaultValue("5s") Duration timeout) {
}
```

## Notes

- Register the type with `@ConfigurationPropertiesScan` or `@EnableConfigurationProperties(PaymentProperties.class)`.
- Add `@Validated` and Jakarta Validation constraints on the record components to fail fast at startup.
