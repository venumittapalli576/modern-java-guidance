---
id: rest-test-client
title: 'Use RestTestClient instead of TestRestTemplate'
category: spring-testing
kind: superseded
applies: boot>=4.0
deprecated: boot>=4.2
note: 'Spring Framework 7 adds RestTestClient, one fluent API for MockMvc-backed and live-server tests. Spring Boot 4.2 deprecates TestRestTemplate together with RestTemplate.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.2.0-M1-Release-Notes
modern-since: 2025
limit: 1
detect:
  - 'code *.java /\bTestRestTemplate\b/'
---

`RestTestClient` is to `TestRestTemplate` what `RestClient` is to `RestTemplate`, with assertions built into the call chain.

## Before

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiTest {

    @Autowired
    TestRestTemplate rest;

    @Test
    void returnsTheOrder() {
        ResponseEntity<Order> response = rest.getForEntity("/orders/42", Order.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## After

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class OrderApiTest {

    @Autowired
    RestTestClient client;

    @Test
    void returnsTheOrder() {
        client.get().uri("/orders/{id}", 42)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .value(order -> assertThat(order.id()).isEqualTo(42));
    }
}
```

## Notes

- On Boot 4, `@SpringBootTest` no longer provides a `TestRestTemplate` bean by default. Code that keeps it needs `@AutoConfigureTestRestTemplate` and the `spring-boot-resttestclient` test dependency.
- The same client runs against `MockMvc` without a server, so one style covers slice tests and full integration tests.
