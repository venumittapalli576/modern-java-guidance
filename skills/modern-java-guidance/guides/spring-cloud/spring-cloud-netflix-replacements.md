---
id: spring-cloud-netflix-replacements
title: 'Use Spring Cloud Gateway, Resilience4j and Spring Cloud LoadBalancer instead of Zuul, Hystrix and Ribbon'
category: spring-cloud
kind: removed
applies: boot>=2.4
note: 'Spring Cloud 2020.0 (for Spring Boot 2.4) removed the Netflix Zuul, Hystrix, Ribbon and Turbine modules, which Netflix had placed in maintenance mode. Eureka is the one that remains.'
source: https://spring.io/blog/2020/12/22/spring-cloud-2020-0-0-aka-ilford-is-available
modern-since: 2020
detect:
  - 'code *.java /@(?:EnableZuulProxy|EnableZuulServer|HystrixCommand|EnableHystrix|EnableHystrixDashboard|EnableCircuitBreaker|EnableTurbine|RibbonClients?)\b/'
  - 'raw pom.xml,build.gradle,build.gradle.kts /spring-cloud-starter-netflix-(?:zuul|hystrix|ribbon|turbine)/'
---

"Spring Cloud microservices" still means Zuul, Hystrix and Ribbon in most tutorials. None of those starters exists for any Spring Boot version from 2.4 on.

## Before

```java
@EnableZuulProxy
@EnableCircuitBreaker
@SpringBootApplication
public class GatewayApplication {
}

@HystrixCommand(fallbackMethod = "cachedStock")
public StockLevel stockFor(String sku) {
    return inventoryClient.stockFor(sku);
}
```

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
</dependency>
```

## After

```java
@CircuitBreaker(name = "inventory", fallbackMethod = "cachedStock")
public StockLevel stockFor(String sku) {
    return inventoryClient.stockFor(sku);
}
```

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: inventory
          uri: lb://inventory-service
          predicates:
            - Path=/inventory/**
```

## Notes

| Removed | Replacement |
|---|---|
| Zuul (`@EnableZuulProxy`) | Spring Cloud Gateway, as a separate gateway application |
| Hystrix (`@HystrixCommand`, `@EnableCircuitBreaker`) | Resilience4j through `spring-cloud-starter-circuitbreaker-resilience4j`, or its own annotations as above |
| Ribbon (`@RibbonClient`) | Spring Cloud LoadBalancer. `@LoadBalanced` and `lb://` URIs keep working |
| Hystrix Dashboard, Turbine | Micrometer metrics with your monitoring system |
