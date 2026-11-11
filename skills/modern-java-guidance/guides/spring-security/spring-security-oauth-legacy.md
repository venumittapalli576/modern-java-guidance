---
id: spring-security-oauth-legacy
title: 'Replace @EnableResourceServer and @EnableAuthorizationServer with Spring Security''s own OAuth2 support'
category: spring-security
kind: deprecated
removed: boot>=3.0
note: 'The spring-security-oauth2 project behind these annotations reached end of life in 2022 and does not run on Spring Boot 3. Use oauth2ResourceServer() and Spring Authorization Server.'
source: https://spring.io/blog/2022/06/01/spring-security-oauth-reaches-end-of-life
modern-since: 2022
detect:
  - 'code *.java /@(?:EnableResourceServer|EnableAuthorizationServer|EnableOAuth2Sso|EnableOAuth2Client)\b/'
  - 'code *.java /^\s*import\s+org\.springframework\.security\.oauth2\.(?:config\.annotation|provider)\./'
  - 'raw pom.xml,build.gradle,build.gradle.kts /spring-security-oauth2-autoconfigure|org\.springframework\.security\.oauth\b/'
---

The old Spring Security OAuth project (`org.springframework.security.oauth`) is the source of `@EnableResourceServer`, `@EnableAuthorizationServer` and `@EnableOAuth2Sso`. It is unmaintained. Resource server and client support live in Spring Security itself, and the authorization server is Spring Authorization Server, which became part of Spring Security in 7.0.

## Before

```java
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
}
```

```xml
<dependency>
    <groupId>org.springframework.security.oauth.boot</groupId>
    <artifactId>spring-security-oauth2-autoconfigure</artifactId>
</dependency>
```

## After

```java
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://idp.example.com/realms/shop
```

## Notes

- Resource server: `spring-boot-starter-oauth2-resource-server` on Boot 3, renamed `spring-boot-starter-security-oauth2-resource-server` on Boot 4.
- Login with an external provider (`@EnableOAuth2Sso`): `http.oauth2Login(Customizer.withDefaults())` with the OAuth2 client starter.
- Issuing tokens yourself (`@EnableAuthorizationServer`): Spring Authorization Server.
