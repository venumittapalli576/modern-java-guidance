---
id: jackson-3-packages
title: 'Use Jackson 3 packages (tools.jackson) on Spring Boot 4'
category: jackson
kind: deprecated
applies: boot>=4.0
note: 'Spring Boot 4 auto-configures Jackson 3, whose packages are tools.jackson.* (annotations stay in com.fasterxml.jackson.annotation). A Jackson 2 ObjectMapper is no longer the bean Boot provides.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
modern-since: 2025
limit: 1
detect:
  - 'code *.java /^\s*import\s+com\.fasterxml\.jackson\.(?!annotation\b)/'
---

Jackson 3 changed its Maven group and Java packages from `com.fasterxml.jackson` to `tools.jackson`. Code written against Jackson 2 compiles only if Jackson 2 is still on the classpath, and then it is a different mapper from the one Spring uses.

## Before

```java
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
class EventCodec {
    private final ObjectMapper objectMapper;

    EventCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String encode(OrderPlaced event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }
}
```

## After

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.json.JsonMapper;

@Service
class EventCodec {
    private final JsonMapper jsonMapper;

    EventCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    String encode(OrderPlaced event) {
        return jsonMapper.writeValueAsString(event);
    }
}
```

## Notes

- `jackson-annotations` is shared by both versions, so `@JsonProperty`, `@JsonIgnore` and the rest keep their `com.fasterxml.jackson.annotation` imports.
- Exceptions are unchecked in Jackson 3. `JsonProcessingException` is replaced by `tools.jackson.core.JacksonException`, and `throws` clauses go away.
- Mappers are immutable. Configure through `JsonMapper.builder()` or, in Boot, a `JsonMapperBuilderCustomizer` bean, which replaces `Jackson2ObjectMapperBuilderCustomizer`. `@JsonComponent` is now `@JacksonComponent`.
- Defining your own `ObjectMapper` bean no longer replaces Boot's mapper. Define a `JsonMapper` bean.
- If a library still needs Jackson 2, the `spring-boot-jackson2` module keeps it working. It is deprecated, and it is a bridge, not a destination.
