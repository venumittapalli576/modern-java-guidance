---
id: security-filter-chain
title: 'Declare a SecurityFilterChain bean instead of extending WebSecurityConfigurerAdapter'
category: spring-security
kind: deprecated
applies: boot>=2.7
removed: boot>=3.0
note: 'WebSecurityConfigurerAdapter was deprecated in Spring Security 5.7 and removed in 6.0 (Spring Boot 3). Security configuration is now a set of beans.'
source: https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
modern-since: 2022
limit: 1
detect:
  - 'code *.java /\bWebSecurityConfigurerAdapter\b/'
---

A security configuration is no longer a class you extend. `configure(HttpSecurity)` becomes a `SecurityFilterChain` bean, and `configure(AuthenticationManagerBuilder)` becomes `UserDetailsService` and `PasswordEncoder` beans.

## Before

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .httpBasic();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
}
```

## After

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/public/**").permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

## Notes

- A `UserDetailsService` bean and a `PasswordEncoder` bean are picked up automatically. There is no `AuthenticationManagerBuilder` step.
- If you need the `AuthenticationManager` itself, inject `AuthenticationConfiguration` and call `getAuthenticationManager()`.
- To ignore paths entirely, declare a `WebSecurityCustomizer` bean. Prefer `permitAll()` so the security headers still apply.
