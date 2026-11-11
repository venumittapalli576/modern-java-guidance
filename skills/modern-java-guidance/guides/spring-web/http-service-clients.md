---
id: http-service-clients
title: 'Prefer HTTP service clients (@HttpExchange) over OpenFeign for new code'
category: spring-web
kind: superseded
applies: boot>=3.0
note: 'Spring Cloud OpenFeign is feature-complete and its documentation points to HTTP service clients, which are built into Spring Framework 6 and need no extra dependency.'
source: https://docs.spring.io/spring-cloud-openfeign/reference/spring-cloud-openfeign.html
modern-since: 2022
limit: 1
detect:
  - 'code *.java /@(?:FeignClient|EnableFeignClients)\b/'
---

Declarative HTTP clients are part of Spring Framework. You write an interface with `@HttpExchange` methods and Spring generates the implementation on top of `RestClient` or `WebClient`.

## Before

```java
@FeignClient(name = "orders", url = "${orders.url}")
public interface OrderClient {

    @GetMapping("/orders/{id}")
    Order findById(@PathVariable("id") long id);
}
```

## After

```java
@HttpExchange("/orders")
public interface OrderClient {

    @GetExchange("/{id}")
    Order findById(@PathVariable long id);
}
```

```java
@Configuration
class OrderClientConfig {

    @Bean
    OrderClient orderClient(RestClient.Builder builder, @Value("${orders.url}") String baseUrl) {
        RestClient restClient = builder.baseUrl(baseUrl).build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(OrderClient.class);
    }
}
```

## Notes

- Spring Boot 4 removes the factory boilerplate: `@ImportHttpServices(group = "orders", types = OrderClient.class)` registers the client, and groups are configured through properties.
- Existing Feign clients keep working. This guide is about what to reach for in new code.
