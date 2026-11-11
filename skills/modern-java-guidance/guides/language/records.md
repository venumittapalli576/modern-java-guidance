---
id: records
title: 'Use records for immutable data carriers instead of Lombok @Value'
category: language
kind: superseded
applies: java>=16
note: 'Records (Java 16) give you the constructor, accessors, equals, hashCode and toString from one line, with no annotation processor.'
source: https://openjdk.org/jeps/395
modern-since: 2021
detect:
  - 'code *.java /^\s*import\s+lombok\.Value\s*;/'
---

A record is the language's own way to say "this type is just its data". DTOs, events, value objects, query projections and configuration properties are all records now.

## Before

```java
import lombok.Value;

@Value
public class Money {
    BigDecimal amount;
    Currency currency;
}
```

## After

```java
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
    }
}
```

## Notes

- Accessors are `amount()` and `currency()`, not `getAmount()`. Jackson, Spring MVC binding, Spring Data projections and `@ConfigurationProperties` all understand records.
- Validation and normalization go in the compact constructor, as above.
- A record cannot extend a class and its fields are final, so JPA entities stay ordinary classes.
- Hand-written classes that are only final fields, a constructor, getters, `equals`, `hashCode` and `toString` are records too.
