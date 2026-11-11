---
id: optional-idioms
title: 'Use Optional''s own methods instead of isPresent() followed by get()'
category: language
kind: superseded
note: 'isPresent() plus get() is a null check with extra steps. map, ifPresent, orElse and orElseThrow say what you mean in one call.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Optional.html
modern-since: 2014
limit: 2
detect:
  - 'code *.java /\bif\s*\(\s*([\w.]+)\.isPresent\(\)\s*\)\s*\{?[^{}]{0,200}?\b\1\.get\(\)/m'
---

## Before

```java
Optional<Customer> customer = customers.findByEmail(email);
if (customer.isPresent()) {
    return customer.get().getName();
}
return "guest";
```

## After

```java
return customers.findByEmail(email)
        .map(Customer::getName)
        .orElse("guest");
```

## Notes

- Fail when absent: `orElseThrow()` (Java 10) or `orElseThrow(() -> new CustomerNotFoundException(email))`.
- Side effect when present: `ifPresent(...)`, or `ifPresentOrElse(...)` (Java 9).
- `Optional` is for return values. Do not use it for fields or parameters.
