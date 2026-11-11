---
id: kafka-default-error-handler
title: 'Use DefaultErrorHandler instead of SeekToCurrentErrorHandler'
category: messaging
kind: removed
applies: boot>=3.0
note: 'SeekToCurrentErrorHandler and the other legacy listener error handlers were deprecated in Spring for Apache Kafka 2.8 and removed in 3.0 (Spring Boot 3). DefaultErrorHandler replaces them.'
source: https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html
modern-since: 2021
detect:
  - 'code *.java /\b(?:SeekToCurrentErrorHandler|SeekToCurrentBatchErrorHandler|RecoveringBatchErrorHandler)\b/'
---

## Before

```java
factory.setErrorHandler(new SeekToCurrentErrorHandler(
        new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 3)));
```

## After

```java
@Bean
DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
    return new DefaultErrorHandler(new DeadLetterPublishingRecoverer(template), new FixedBackOff(1000L, 3));
}
```

## Notes

- Spring Boot applies a single `CommonErrorHandler` bean to the auto-configured listener container factory. When you build the factory yourself, call `factory.setCommonErrorHandler(...)`.
- `DefaultErrorHandler` serves record and batch listeners alike.
- For retries that do not block the partition, use `@RetryableTopic`.
