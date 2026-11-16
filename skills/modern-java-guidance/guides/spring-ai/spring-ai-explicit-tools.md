---
id: spring-ai-explicit-tools
title: 'Pass tools to ChatClient explicitly'
category: spring-ai
kind: removed
applies: boot>=4.0
note: 'Spring AI 2.0 removed toolNames(), SpringBeanToolCallbackResolver and internalToolExecutionEnabled. ChatModel no longer runs tools itself: ChatClient does it through ToolCallingAdvisor.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2026
detect:
  - 'code *.java /\.toolNames\s*\(|\.internalToolExecutionEnabled\s*\(|\bSpringBeanToolCallbackResolver\b|\bToolExecutionEligibilityPredicate\b|\bToolCallAdvisor\b/'
  - 'code application*.properties,application*.yml,application*.yaml /^spring\.ai\.[\w.-]+\.internal-tool-execution-enabled\b/'
---

In Spring AI 1 a tool could be named by a string and looked up as a bean, and each `ChatModel` ran its own tool loop. Spring AI 2 moved the loop into one advisor and made the tool list explicit. The security release that followed (2.0.1) went further: a tool the request did not declare is no longer resolved at all.

## Before

```java
OpenAiChatOptions options = OpenAiChatOptions.builder()
        .toolNames("currentWeather")
        .internalToolExecutionEnabled(false)
        .build();
```

```properties
spring.ai.openai.chat.options.internal-tool-execution-enabled=false
```

## After

```java
String answer = chatClient.prompt()
        .user(question)
        .tools(weatherTools)
        .toolContext(Map.of("tenantId", tenantId))
        .call()
        .content();
```

## Notes

- `ChatClient` registers `ToolCallingAdvisor` on its own. Remove any `.advisors(ToolCallingAdvisor.builder().build())`.
- `ChatModel.call(...)` now returns the raw tool-call response. If you call a `ChatModel` directly, drive the loop with `ToolCallingManager`, or use `ChatClient`.
- `ToolCallAdvisor` was renamed `ToolCallingAdvisor`, and `ToolExecutionEligibilityPredicate` became `ToolExecutionEligibilityChecker`.
- `tools(t -> t.callbacks(cb).context(k, v))` is gone: write `.tools(cb).toolContext(Map.of(k, v))`.
- Since 2.0.1 a conversation may call one tool 40 times and all tools 150 times before the manager stops it. Raise the limits with `spring.ai.tools.limits.*` if an agent loop legitimately needs more.
