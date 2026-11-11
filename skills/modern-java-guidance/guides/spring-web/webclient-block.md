---
id: webclient-block
title: 'Use RestClient instead of WebClient.block() in servlet applications'
category: spring-web
kind: superseded
applies: boot>=3.2
note: 'WebClient plus block() was the stopgap for a fluent client in Spring MVC apps. RestClient gives the same fluent API without Reactor types or a WebFlux dependency.'
source: https://docs.spring.io/spring-framework/reference/integration/rest-clients.html
modern-since: 2023
detect:
  - 'code *.java /\.(?:bodyToMono|bodyToFlux|toEntity|toBodilessEntity)\([^;]*\.block(?:First|Last)?\(\s*\)/m'
---

Before Spring Framework 6.1, the only fluent HTTP client was the reactive one, so blocking applications called `WebClient` and then `.block()`. That pulls Reactor Netty into a servlet app and hides a blocking call behind a reactive type.

## Before

```java
public Quote latestQuote(String symbol) {
    return webClient.get()
            .uri("/quotes/{symbol}", symbol)
            .retrieve()
            .bodyToMono(Quote.class)
            .block();
}
```

## After

```java
public Quote latestQuote(String symbol) {
    return restClient.get()
            .uri("/quotes/{symbol}", symbol)
            .retrieve()
            .body(Quote.class);
}
```

## Notes

- Keep `WebClient` when the application is reactive end to end, or when you need streaming or many concurrent calls composed with Reactor operators.
- With virtual threads enabled (`spring.threads.virtual.enabled=true`, Boot 3.2+), a blocking `RestClient` call no longer ties up a platform thread.
