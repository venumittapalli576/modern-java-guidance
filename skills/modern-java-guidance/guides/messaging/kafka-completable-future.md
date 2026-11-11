---
id: kafka-completable-future
title: 'Handle KafkaTemplate results with CompletableFuture, not ListenableFuture'
category: messaging
kind: removed
applies: boot>=3.0
note: 'Spring for Apache Kafka 3.0 (Spring Boot 3) changed KafkaTemplate.send() to return CompletableFuture. Spring''s ListenableFuture was deprecated in Framework 6 and removed in 7.'
source: https://docs.spring.io/spring-kafka/reference/kafka/sending-messages.html
modern-since: 2022
when-file: 'org\.springframework\.util\.concurrent|\bKafkaTemplate\b|\bSendResult\b|\bAsyncRestTemplate\b'
detect:
  - 'code *.java /\bListenableFuture(?:Callback)?\b/'
  - 'code *.java /(?<!Futures)\.addCallback\s*\(/'
---

## Before

```java
ListenableFuture<SendResult<String, OrderPlaced>> future = kafkaTemplate.send("orders", order.id(), event);
future.addCallback(
        result -> log.debug("Published order {}", order.id()),
        ex -> log.error("Publishing order {} failed", order.id(), ex));
```

## After

```java
kafkaTemplate.send("orders", order.id(), event)
        .whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Publishing order {} failed", order.id(), ex);
            } else {
                log.debug("Published order {}", order.id());
            }
        });
```

## Notes

- To wait for the broker acknowledgement, call `.get(10, TimeUnit.SECONDS)` on the future.
- Guava's own `ListenableFuture` is a different class and is not affected. This guide only fires in files that use Spring's.
