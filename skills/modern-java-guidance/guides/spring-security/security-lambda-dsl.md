---
id: security-lambda-dsl
title: 'Configure HttpSecurity with the lambda DSL, without and()'
category: spring-security
kind: superseded
applies: boot>=2.7
deprecated: boot>=3.1
removed: boot>=4.0
note: 'The chained style, csrf().disable() joined with and(), was deprecated in Spring Security 6.1 and removed in 7.0 (Spring Boot 4). Each configurer takes a lambda, available since 5.2.'
source: https://docs.spring.io/spring-security/reference/migration-7/configuration.html
modern-since: 2019
when-file: '\b(?:Server)?HttpSecurity\b'
detect:
  - 'code *.java /\.and\(\)/'
  - 'code *.java /\.(?:csrf|cors|httpBasic|formLogin|logout|sessionManagement|exceptionHandling|headers|oauth2Login|oauth2ResourceServer|oauth2Client|requestCache|securityContext|x509|saml2Login|authorizeHttpRequests)\(\s*\)/'
---

In Spring Security 7 the no-argument configurer methods and `and()` do not exist. Every section of the configuration is a lambda, so the indentation shows what is being configured and nothing needs to be chained back to the parent.

## Before

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeHttpRequests()
            .requestMatchers("/login").permitAll()
            .anyRequest().authenticated()
        .and()
        .oauth2ResourceServer().jwt();
    return http.build();
}
```

## After

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/login").permitAll()
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    return http.build();
}
```

## Notes

- `Customizer.withDefaults()` turns a feature on with its defaults: `.httpBasic(Customizer.withDefaults())`, `.cors(Customizer.withDefaults())`.
- Custom DSLs use `http.with(new MyConfigurer(), Customizer.withDefaults())`. The older `http.apply(...)` was removed along with `and()`.
