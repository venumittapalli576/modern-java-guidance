---
id: authorize-http-requests
title: 'Use authorizeHttpRequests instead of authorizeRequests'
category: spring-security
kind: superseded
applies: boot>=2.7
deprecated: boot>=3.1
removed: boot>=4.0
note: 'authorizeRequests() was deprecated in Spring Security 6 and removed in 7.0 (Spring Boot 4). authorizeHttpRequests() is its replacement, built on the AuthorizationManager API.'
source: https://docs.spring.io/spring-security/reference/7.0/whats-new.html
modern-since: 2021
detect:
  - 'code *.java /\.authorizeRequests\s*\(/'
---

`authorizeRequests` is the old `FilterSecurityInterceptor` path. `authorizeHttpRequests` uses `AuthorizationFilter`, and is the only one left in Spring Security 7.

## Before

```java
http.authorizeRequests(authorize -> authorize
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated());
```

## After

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated());
```

## Notes

- `access("hasRole('ADMIN') and hasIpAddress('10.0.0.0/8')")` takes an `AuthorizationManager` now. Use `access(new WebExpressionAuthorizationManager("..."))` to keep a SpEL expression, or compose managers with `AuthorizationManagers.allOf(...)`.
- `authorizeHttpRequests` authorizes every dispatcher type by default, including `FORWARD` and `ERROR`. Add `dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()` if a forward to a view or the error page starts returning 403.
