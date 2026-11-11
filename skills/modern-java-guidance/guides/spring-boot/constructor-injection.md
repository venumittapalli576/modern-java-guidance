---
id: constructor-injection
title: 'Inject dependencies through the constructor, not @Autowired fields'
category: spring-boot
kind: superseded
note: 'Field injection hides dependencies and rules out final fields. Since Spring 4.3 a class with a single constructor needs no @Autowired at all.'
source: https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html
modern-since: 2016
when-file: '\A(?![\s\S]*@(?:SpringBootTest|WebMvcTest|DataJpaTest|JsonTest|RestClientTest|WebFluxTest|JdbcTest|ExtendWith|SpringJUnitConfig|SpringJUnitWebConfig|RunWith|Test)\b)'
detect:
  - 'code *.java /@Autowired\s*(?:\([^)]*\)\s*)?(?:@\w+(?:\([^)]*\))?\s*)*(?:private\s+|protected\s+|public\s+)?(?!static\b)[\w.<>\[\], ?]+\s+\w+\s*;/m'
---

Constructor injection makes a component's dependencies explicit, lets the fields be `final`, and lets you build the object in a unit test with plain `new`.

## Before

```java
@Service
public class OrderService {

    @Autowired
    private OrderRepository orders;

    @Autowired
    @Qualifier("primary")
    private PaymentGateway payments;
}
```

## After

```java
@Service
public class OrderService {

    private final OrderRepository orders;
    private final PaymentGateway payments;

    public OrderService(OrderRepository orders, @Qualifier("primary") PaymentGateway payments) {
        this.orders = orders;
        this.payments = payments;
    }
}
```

## Notes

- Test classes are exempt. Field injection of `MockMvc` or a repository in a `@SpringBootTest` is normal.
- If you already use Lombok, `@RequiredArgsConstructor` on a class with `final` fields gives the same result.
- A long constructor is a signal that the class does too much. Field injection only hides that.
