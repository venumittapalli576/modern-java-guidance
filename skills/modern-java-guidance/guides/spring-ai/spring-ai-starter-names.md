---
id: spring-ai-starter-names
title: 'Use the Spring AI 1.0 starter names'
category: spring-ai
kind: removed
note: 'Spring AI 1.0.0-M7 renamed every starter: spring-ai-openai-spring-boot-starter became spring-ai-starter-model-openai. The old artifact ids stop at 1.0.0-M6 and do not resolve for any GA release.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2025
detect:
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /spring-ai-[\w-]+-spring-boot-starter(?![\w-])/'
---

Most Spring AI tutorials were written during the milestone releases, so this is the dependency an agent reaches for first. With a 1.0 or 2.0 BOM the build fails with "could not find artifact".

## Before

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-pgvector-store-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-client-spring-boot-starter</artifactId>
</dependency>
```

## After

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
```

## Notes

| Milestone name | Name since 1.0.0-M7 |
|---|---|
| `spring-ai-{model}-spring-boot-starter` | `spring-ai-starter-model-{model}` |
| `spring-ai-{store}-store-spring-boot-starter` | `spring-ai-starter-vector-store-{store}` |
| `spring-ai-mcp-{type}-spring-boot-starter` | `spring-ai-starter-mcp-{type}` |

- Import `org.springframework.ai:spring-ai-bom` and leave the versions off the starters.
- Spring AI 1.x runs on Spring Boot 3.4 and 3.5. Spring AI 2.x needs Spring Boot 4.
