---
id: spring-ai-message-chat-memory-advisor
title: 'Use MessageChatMemoryAdvisor and pass the conversation id per call'
category: spring-ai
kind: removed
applies: boot>=4.0
note: 'Spring AI 2.0 removed PromptChatMemoryAdvisor, ChatMemory.DEFAULT_CONVERSATION_ID and conversationId() on the advisor builders. Use MessageChatMemoryAdvisor and pass the id on each call.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2026
detect:
  - 'code *.java /\bPromptChatMemoryAdvisor\b|\bChatMemory\.DEFAULT_CONVERSATION_ID\b/'
  - 'code *.java /ChatMemoryAdvisor\s*\.\s*builder\s*\([^;]*?\.conversationId\s*\(/m'
---

Spring AI 2 keeps one memory advisor for chat history and makes the conversation explicit. An advisor that is built once with a fixed conversation id shares one history between every caller.

## Before

```java
ChatClient chatClient = builder
        .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
        .build();
```

```java
MessageChatMemoryAdvisor advisor = MessageChatMemoryAdvisor.builder(chatMemory)
        .conversationId("support")
        .build();
```

## After

```java
ChatClient chatClient = builder
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .build();

String reply = chatClient.prompt()
        .user(message)
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
        .call()
        .content();
```

## Notes

- `MessageChatMemoryAdvisor` has the same builder as the removed class. History is sent as messages, not pasted into the system prompt.
- The memory advisor now runs outside the tool loop by default (`HIGHEST_PRECEDENCE + 200`, with `ToolCallingAdvisor` at `+ 300`). Intermediate tool messages are no longer written to memory unless you reorder them.
- `JdbcChatMemoryRepository` needs a schema change on upgrade: the `SPRING_AI_CHAT_MEMORY` table gains a `sequence_id` column. The migration SQL is in the upgrade notes.
