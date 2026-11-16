---
id: spring-ai-model-selection
title: 'Select a model provider with spring.ai.model.*'
category: spring-ai
kind: removed
note: 'Spring AI 1.0.0-M7 removed spring.ai.<provider>.chat.enabled and its siblings. Choose the provider with spring.ai.model.chat=openai, or switch a model type off with spring.ai.model.chat=none.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2025
detect:
  - 'code application*.properties,application*.yml,application*.yaml /^spring\.ai\.[\w-]+\.(?:chat|embedding|image|audio\.speech|audio\.transcription|moderation)\.enabled\s*[:=]/'
---

The old switches are ignored without a warning. An application with two providers on the classpath and `enabled=false` on one of them now gets two `ChatModel` beans and fails to start, or silently talks to the wrong provider.

## Before

```properties
spring.ai.openai.chat.enabled=false
spring.ai.ollama.embedding.enabled=true
```

## After

```properties
spring.ai.model.chat=anthropic
spring.ai.model.embedding=ollama
```

## Notes

- One key per model type: `spring.ai.model.chat`, `spring.ai.model.embedding`, `spring.ai.model.image` and so on. The value is the provider name, or `none`.
- With a single provider on the classpath you need none of this.
