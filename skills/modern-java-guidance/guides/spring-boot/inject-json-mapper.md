---
id: inject-json-mapper
title: 'Inject the JSON mapper Spring Boot configured instead of calling new ObjectMapper()'
category: spring-boot
kind: hazard
note: 'A hand-built ObjectMapper ignores spring.jackson.* settings, registered modules and customizers, so it reads and writes dates and property names differently from the rest of the application.'
source: https://docs.spring.io/spring-boot/reference/features/json.html
modern-since: 2014
when-file: '@(?:Service|Component|RestController|Controller|Repository|Configuration)\b'
detect:
  - 'code *.java /\bnew\s+ObjectMapper\s*\(\s*\)/'
---

Spring Boot builds one mapper from your configuration and uses it for HTTP messages. A second mapper created with `new` inside a bean shares none of that, which shows up as timestamps in one place and ISO dates in another.

## Before

```java
@Service
public class AuditLog {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String describe(AuditEvent event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }
}
```

## After

```java
@Service
public class AuditLog {
    private final JsonMapper jsonMapper;

    public AuditLog(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String describe(AuditEvent event) {
        return jsonMapper.writeValueAsString(event);
    }
}
```

## Notes

- On Spring Boot 4 the bean is a Jackson 3 `tools.jackson.databind.json.JsonMapper`, and its exceptions are unchecked. On Boot 3 inject `com.fasterxml.jackson.databind.ObjectMapper`.
- To change the mapper, add a `JsonMapperBuilderCustomizer` bean (Boot 4) or a `Jackson2ObjectMapperBuilderCustomizer` bean (Boot 3) instead of building your own.
- Outside Spring-managed classes, such as a static utility or a plain unit test, creating a mapper is fine. This guide only fires in annotated components.
