---
id: mockito-bean
title: 'Use @MockitoBean and @MockitoSpyBean instead of @MockBean and @SpyBean'
category: spring-testing
kind: deprecated
applies: boot>=3.4
removed: boot>=4.0
note: 'Spring Boot 3.4 deprecated @MockBean and @SpyBean in favor of Spring Framework''s @MockitoBean and @MockitoSpyBean. Spring Boot 4 removed them.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
modern-since: 2024
detect:
  - 'code *.java /@(?:MockBean|SpyBean)\b/'
  - 'code *.java /^\s*import\s+org\.springframework\.boot\.test\.mock\.mockito\./'
---

Bean overriding in tests moved from Spring Boot into Spring Framework 6.2. The annotations were renamed on the way.

## Before

```java
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderService orderService;
}
```

## After

```java
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OrderService orderService;
}
```

## Notes

- `@MockitoBean` works on test class fields, not inside `@Configuration` classes. To share mocks across tests, put `@MockitoBean(types = {OrderService.class, PaymentGateway.class})` on the test class or on a meta-annotation.
- Boot 4 also removed `MockitoTestExecutionListener`. If plain `@Mock` or `@Captor` fields stop being initialized, add `@ExtendWith(MockitoExtension.class)`.
