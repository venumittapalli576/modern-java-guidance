---
id: testcontainers-service-connection
title: 'Wire Testcontainers with @ServiceConnection instead of @DynamicPropertySource'
category: spring-testing
kind: superseded
applies: boot>=3.1
note: 'Since Spring Boot 3.1, @ServiceConnection reads connection details straight from the container, so the URL, username and password no longer have to be copied into properties by hand.'
source: https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
modern-since: 2023
detect:
  - 'code *.java /@DynamicPropertySource\b/'
---

The property-copying method is boilerplate that has to match the property names of whatever driver or client you use. `@ServiceConnection` removes it.

## Before

```java
@SpringBootTest
@Testcontainers
class OrderRepositoryTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

## After

```java
@SpringBootTest
@Testcontainers
class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");
}
```

## Notes

- Needs the `spring-boot-testcontainers` dependency.
- Keep `@DynamicPropertySource` for a container Spring Boot has no connection support for, or for properties that are not connection details.
- On Testcontainers 1.x the field type is written `PostgreSQLContainer<?>`. Testcontainers 2 dropped the type parameter.
