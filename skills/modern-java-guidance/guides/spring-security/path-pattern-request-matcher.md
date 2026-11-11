---
id: path-pattern-request-matcher
title: 'Use PathPatternRequestMatcher instead of AntPathRequestMatcher and MvcRequestMatcher'
category: spring-security
kind: deprecated
applies: boot>=3.5
removed: boot>=4.0
note: 'AntPathRequestMatcher and MvcRequestMatcher were deprecated in Spring Security 6.5 and removed in 7.0 (Spring Boot 4). PathPatternRequestMatcher replaces both.'
source: https://docs.spring.io/spring-security/reference/migration-7/web.html
modern-since: 2025
limit: 1
detect:
  - 'code *.java /\b(?:AntPathRequestMatcher|MvcRequestMatcher)\b/'
---

Spring Security 7 standardizes on Spring's `PathPattern` syntax. Inside the DSL you rarely build a matcher at all: `requestMatchers("/api/**")` already does the right thing.

## Before

```java
http.securityMatcher(new AntPathRequestMatcher("/api/**"))
    .logout(logout -> logout.logoutRequestMatcher(new AntPathRequestMatcher("/signout", "POST")));
```

## After

```java
http.securityMatcher("/api/**")
    .logout(logout -> logout.logoutRequestMatcher(
        PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/signout")));
```

## Notes

- Patterns are relative to the context path and must start with `/`.
- `PathPattern` only allows `**` at the end of a pattern. An Ant pattern such as `/**/*.css` has to be rewritten, for example as `/css/**`.
