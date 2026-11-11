---
id: framework-retry
title: 'Use Spring Framework''s built-in @Retryable instead of Spring Retry'
category: spring-boot
kind: superseded
applies: boot>=4.0
note: 'Spring Framework 7 has retry and concurrency limits in the core (org.springframework.resilience.annotation, org.springframework.core.retry). Spring Retry is superseded and Boot 4 no longer manages its version.'
source: https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes
modern-since: 2025
limit: 1
detect:
  - 'code *.java /^\s*import\s+org\.springframework\.retry\./'
  - 'raw pom.xml,build.gradle,build.gradle.kts /org\.springframework\.retry\b/'
---

The retry features that lived in the separate Spring Retry project moved into Spring Framework itself. No extra dependency, and no AspectJ starter just for `@Retryable`.

## Before

```java
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;

@Configuration
@EnableRetry
class RetryConfig {
}
```

```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
```

## After

```java
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;

@Configuration
@EnableResilientMethods
class ResilienceConfig {
}

@Service
class PaymentGateway {

    @Retryable
    @ConcurrencyLimit(10)
    Receipt charge(Order order) {
        return client.charge(order);
    }
}
```

## Notes

- Spring Boot does not switch this on for you. `@EnableResilientMethods` is required.
- The number of retries, delay, jitter, multiplier and the exception types to retry on are attributes of `@Retryable`. For programmatic use there is `RetryTemplate` in `org.springframework.core.retry`.
- This covers retry and concurrency limits. For circuit breakers, bulkheads and rate limiters keep Resilience4j.
