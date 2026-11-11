---
id: rest-client
title: 'Use RestClient instead of RestTemplate'
category: spring-web
kind: superseded
applies: boot>=3.2
deprecated: boot>=4.2
note: 'RestClient (Spring Framework 6.1) is the replacement for blocking HTTP calls. RestTemplate is deprecated for removal as of Spring Framework 7.1 (Spring Boot 4.2) and is removed in 8.0.'
source: https://spring.io/blog/2025/09/30/the-state-of-http-clients-in-spring/
modern-since: 2023
limit: 1
detect:
  - 'code *.java /\bRestTemplate(?:Builder)?\b/'
---

`RestTemplate` has been feature-complete for years and none of the newer client features reach it. `RestClient` has a fluent API, the same underlying HTTP libraries, and the same message converters and interceptors.

## Before

```java
@Service
public class InventoryClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public StockLevel stockFor(String sku) {
        ResponseEntity<StockLevel> response = restTemplate.getForEntity(
                "https://inventory.example.com/stock/{sku}", StockLevel.class, sku);
        return response.getBody();
    }
}
```

## After

```java
@Service
public class InventoryClient {
    private final RestClient restClient;

    public InventoryClient(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("https://inventory.example.com").build();
    }

    public StockLevel stockFor(String sku) {
        return restClient.get()
                .uri("/stock/{sku}", sku)
                .retrieve()
                .body(StockLevel.class);
    }
}
```

## Notes

- Inject `RestClient.Builder` so the client gets Boot's configured converters, observability and SSL bundles. On Spring Boot 4 the builder comes from `spring-boot-starter-restclient`.
- To move one call site at a time, `RestClient.create(restTemplate)` wraps an existing `RestTemplate` and reuses its request factory, interceptors and converters.
- For streaming or a fully reactive stack, use `WebClient`.
