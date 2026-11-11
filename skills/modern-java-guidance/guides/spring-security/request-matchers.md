---
id: request-matchers
title: 'Use requestMatchers instead of antMatchers, mvcMatchers and regexMatchers'
category: spring-security
kind: removed
applies: boot>=3.0
note: 'antMatchers(), mvcMatchers() and regexMatchers() were deprecated in Spring Security 5.8 and removed in 6.0 (Spring Boot 3). requestMatchers() picks the right matcher for the application.'
source: https://docs.spring.io/spring-security/reference/5.8/migration/servlet/config.html
modern-since: 2022
detect:
  - 'code *.java /\.(?:ant|mvc|regex)Matchers\s*\(/'
---

The three matcher methods collapsed into one. `requestMatchers` uses Spring MVC path matching when MVC is present, which closes a class of bypasses where the security rule and the controller mapping disagreed about a path.

## Before

```java
http.authorizeHttpRequests(authorize -> authorize
        .antMatchers(HttpMethod.GET, "/products/**").permitAll()
        .mvcMatchers("/orders/**").hasRole("USER")
        .regexMatchers("/reports/[0-9]+").hasRole("ANALYST")
        .anyRequest().authenticated());
```

## After

```java
http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
        .requestMatchers("/orders/**").hasRole("USER")
        .requestMatchers(RegexRequestMatcher.regexMatcher("/reports/[0-9]+")).hasRole("ANALYST")
        .anyRequest().authenticated());
```

## Notes

- The same rename applies to `securityMatcher(...)`, which replaced `antMatcher(...)` and `mvcMatcher(...)` on `HttpSecurity`, and to `csrf.ignoringRequestMatchers(...)`.
