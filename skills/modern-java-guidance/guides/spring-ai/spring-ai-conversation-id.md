---
id: spring-ai-conversation-id
title: 'Use ChatMemory.CONVERSATION_ID for the conversation id'
category: spring-ai
kind: removed
note: 'Spring AI 1.0.0-RC1 renamed CHAT_MEMORY_CONVERSATION_ID_KEY to ChatMemory.CONVERSATION_ID and CHAT_MEMORY_RETRIEVE_SIZE_KEY to TOP_K. The old constants were removed.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2025
detect:
  - 'code *.java /\bCHAT_MEMORY_CONVERSATION_ID_KEY\b|\bCHAT_MEMORY_RETRIEVE_SIZE_KEY\b/'
---

Chat memory is keyed by a conversation id that travels in the advisor context. The constant that names it changed just before 1.0.

## Before

```java
String reply = chatClient.prompt()
        .user(message)
        .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
        .call()
        .content();
```

## After

```java
String reply = chatClient.prompt()
        .user(message)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
        .call()
        .content();
```

## Notes

- `ChatMemory` is `org.springframework.ai.chat.memory.ChatMemory`.
- `VectorStoreChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY` is now `TOP_K`, and its default dropped from 100 to 20.
- On Spring AI 2 the id is mandatory. There is no `"default"` conversation any more, and a call without the parameter throws `IllegalArgumentException`. Two users sharing one default conversation was a data leak waiting to happen.
