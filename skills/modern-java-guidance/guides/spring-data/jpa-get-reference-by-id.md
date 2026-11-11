---
id: jpa-get-reference-by-id
title: 'Use getReferenceById instead of getOne and getById'
category: spring-data
kind: deprecated
applies: boot>=2.7
note: 'JpaRepository.getOne() and getById() are deprecated in favor of getReferenceById() (Spring Data JPA 2.7), whose name says what it returns: a lazy reference, with no database query.'
source: https://docs.spring.io/spring-data/jpa/docs/current/api/org/springframework/data/jpa/repository/JpaRepository.html
modern-since: 2022
detect:
  - 'code *.java /\b\w*[rR]epo(?:sitory)?\w*\.(?:getOne|getById)\s*\(/'
---

`getOne` and `getById` look like lookups but return an uninitialized proxy. The first property access outside a transaction throws `LazyInitializationException`, and a missing row surfaces as `EntityNotFoundException` far from the call.

## Before

```java
Customer customer = customerRepository.getOne(customerId);
order.setCustomer(customer);
```

## After

```java
Customer customer = customerRepository.getReferenceById(customerId);
order.setCustomer(customer);
```

## Notes

- Use `getReferenceById` only to set an association without loading the row, as above.
- When you need the entity's data, use `findById(id).orElseThrow()`.
