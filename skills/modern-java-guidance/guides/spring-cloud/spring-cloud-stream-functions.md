---
id: spring-cloud-stream-functions
title: 'Write Spring Cloud Stream bindings as functions, not @StreamListener'
category: spring-cloud
kind: removed
applies: boot>=3.0
note: '@EnableBinding, @StreamListener and the Source, Sink and Processor interfaces were deprecated in Spring Cloud Stream 3.1 and removed in 4.0 (Spring Boot 3). Bindings are Supplier, Function and Consumer beans.'
source: https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/producing-and-consuming-messages.html
modern-since: 2019
detect:
  - 'code *.java /@(?:EnableBinding|StreamListener)\b/'
---

## Before

```java
@EnableBinding(Sink.class)
public class OrderEvents {

    @StreamListener(Sink.INPUT)
    public void onOrderPlaced(OrderPlaced event) {
        fulfilment.start(event);
    }
}
```

## After

```java
@Configuration
public class OrderEvents {

    @Bean
    Consumer<OrderPlaced> orderPlaced(FulfilmentService fulfilment) {
        return fulfilment::start;
    }
}
```

```properties
spring.cloud.function.definition=orderPlaced
spring.cloud.stream.bindings.orderPlaced-in-0.destination=orders
```

## Notes

- Binding names follow `<function>-in-<index>` and `<function>-out-<index>`.
- To send from imperative code such as a REST controller, inject `StreamBridge` and call `streamBridge.send("orders-out-0", event)`.
