---
id: enable-method-security
title: 'Use @EnableMethodSecurity instead of @EnableGlobalMethodSecurity'
category: spring-security
kind: superseded
applies: boot>=2.7
deprecated: boot>=3.0
note: '@EnableGlobalMethodSecurity has been deprecated since Spring Security 5.8. @EnableMethodSecurity turns on @PreAuthorize and @PostAuthorize by default, so prePostEnabled = true is no longer needed.'
source: https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
modern-since: 2021
detect:
  - 'code *.java /@EnableGlobalMethodSecurity\b/'
---

`@EnableMethodSecurity` is the `AuthorizationManager`-based replacement. Pre and post annotations are on by default; `@Secured` and JSR-250 stay opt-in.

## Before

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig {
}
```

## After

```java
@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class MethodSecurityConfig {
}
```

## Notes

- If you extended `GlobalMethodSecurityConfiguration` to customize expression handling, publish a `MethodSecurityExpressionHandler` bean instead.
