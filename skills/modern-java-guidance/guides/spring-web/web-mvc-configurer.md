---
id: web-mvc-configurer
title: 'Implement WebMvcConfigurer instead of extending WebMvcConfigurerAdapter'
category: spring-web
kind: deprecated
removed: boot>=3.0
note: 'WebMvcConfigurerAdapter was deprecated in Spring Framework 5.0, when WebMvcConfigurer gained default methods, and removed in 6.0 (Spring Boot 3).'
source: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/customize.html
modern-since: 2017
detect:
  - 'code *.java /\bWebMvcConfigurerAdapter\b/'
---

The adapter class existed only so you did not have to implement every method of the interface. Java 8 default methods made it redundant.

## Before

```java
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("https://app.example.com");
    }
}
```

## After

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("https://app.example.com");
    }
}
```

## Notes

- Do not add `@EnableWebMvc` in a Spring Boot application. It switches off Boot's MVC auto-configuration.
- `HandlerInterceptorAdapter` went the same way: implement `HandlerInterceptor` directly.
