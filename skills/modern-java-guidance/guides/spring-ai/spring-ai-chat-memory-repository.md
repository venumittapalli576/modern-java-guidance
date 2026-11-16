---
id: spring-ai-chat-memory-repository
title: 'Use the chat memory repository modules and keys'
category: spring-ai
kind: removed
note: 'Spring AI 1.0.0-RC1 renamed the chat memory modules and keys: spring-ai-starter-model-chat-memory-jdbc gained -repository-, and spring.ai.chat.memory.jdbc.* moved under spring.ai.chat.memory.repository.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2025
detect:
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /spring-ai-(?:starter-|autoconfigure-)?model-chat-memory-(?:jdbc|cassandra|neo4j)(?![\w-])/'
  - 'code application*.properties,application*.yml,application*.yaml /^spring\.ai\.chat\.memory\.(?:jdbc|cassandra|neo4j)\./'
---

Spring AI 1.0 split chat memory into a `ChatMemory` (the policy, such as a message window) and a `ChatMemoryRepository` (the storage). The storage modules were renamed to say so.

## Before

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-jdbc</artifactId>
</dependency>
```

```properties
spring.ai.chat.memory.jdbc.initialize-schema=always
```

## After

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
</dependency>
```

```properties
spring.ai.chat.memory.repository.jdbc.initialize-schema=always
```

## Notes

- Java packages gained the same segment: `org.springframework.ai.chat.memory.jdbc` is now `org.springframework.ai.chat.memory.repository.jdbc`.
- `CassandraChatMemory` was removed. Use `CassandraChatMemoryRepository` behind a `MessageWindowChatMemory`.
- Redis followed in Spring AI 2.0.1: `spring.ai.chat.memory.redis.*` is now `spring.ai.chat.memory.repository.redis.*`. The old Redis module still works for now.
