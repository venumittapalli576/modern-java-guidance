---
id: spring-ai-options-properties
title: 'Drop .options from Spring AI configuration keys'
category: spring-ai
kind: deprecated
applies: boot>=4.0
note: 'Spring AI 2.0 dropped the .options segment: spring.ai.openai.chat.options.model is now spring.ai.openai.chat.model. The old keys are deprecated. The implicit temperature of 0.7 is gone too.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2026
detect:
  - 'code application*.properties,application*.yml,application*.yaml /^spring\.ai\.[\w.-]+\.options\.[\w-]+\s*[:=]/'
---

Model options are configured directly under the model's prefix in Spring AI 2.

## Before

```properties
spring.ai.openai.chat.options.temperature=0.2
spring.ai.openai.embedding.options.model=text-embedding-3-small
```

## After

```properties
spring.ai.openai.chat.temperature=0.2
spring.ai.openai.embedding.model=text-embedding-3-small
```

## Notes

- Spring AI 1 sent `temperature=0.7` when you set nothing. Spring AI 2 sends nothing, so the provider's own default applies. If output changed after an upgrade, set the temperature explicitly.
- In Java, `properties.getOptions().getModel()` becomes `properties.getModel()`.
- Options objects are immutable in 2.0. `copy()` and `fromOptions(...)` are gone: use `mutate()`. `ChatClient.options(...)` takes the builder, not a built object.
- `spring.ai.ollama.chat.think-option` was renamed `spring.ai.ollama.chat.think`.
