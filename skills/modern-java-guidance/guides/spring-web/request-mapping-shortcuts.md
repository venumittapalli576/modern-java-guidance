---
id: request-mapping-shortcuts
title: 'Use @GetMapping, @PostMapping and friends instead of @RequestMapping(method = ...)'
category: spring-web
kind: superseded
note: 'Composed mapping annotations have been in Spring MVC since 4.3 (2016). They are shorter and make the HTTP method impossible to miss.'
source: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html
modern-since: 2016
detect:
  - 'code *.java /@RequestMapping\s*\([^)]*\bmethod\s*=\s*(?:RequestMethod\.)?(?:GET|POST|PUT|DELETE|PATCH)\b/m'
---

`@RequestMapping` belongs on the class, for the shared path prefix. Handler methods use the composed annotations.

## Before

```java
@RequestMapping(value = "/orders/{id}", method = RequestMethod.GET)
public Order find(@PathVariable long id) {
    return orders.find(id);
}
```

## After

```java
@GetMapping("/orders/{id}")
public Order find(@PathVariable long id) {
    return orders.find(id);
}
```

## Notes

- A method-level `@RequestMapping` without `method` matches every HTTP method, which is rarely intended.
- A handler that really serves several methods keeps `@RequestMapping(method = {GET, POST})`. This guide leaves that form alone.
