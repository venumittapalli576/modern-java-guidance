---
id: spring-ai-anthropic-builder
title: 'Build AnthropicChatModel with its builder, not AnthropicApi'
category: spring-ai
kind: removed
applies: boot>=4.0
note: 'Spring AI 2.0 moved to the official anthropic-java SDK and removed AnthropicApi and the AnthropicChatModel(AnthropicApi, ...) constructor. Build the model with AnthropicChatModel.builder().'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2026
detect:
  - 'code *.java /\bAnthropicApi\b/'
---

The hand-written `AnthropicApi` client and its nested request records are gone. Anything that built requests against them needs the builder or the SDK's own types.

## Before

```java
AnthropicApi api = AnthropicApi.builder().apiKey(apiKey).build();
AnthropicChatModel model = new AnthropicChatModel(api, options);
```

## After

```java
AnthropicChatModel model = AnthropicChatModel.builder()
        .apiKey(apiKey)
        .defaultOptions(options)
        .build();
```

## Notes

- With the starter you rarely build the model yourself: inject `ChatClient.Builder` and set `spring.ai.anthropic.api-key`.
- The default `maxTokens` went from 500 to 4096. A response that used to be cut off may now be eight times longer, and cost accordingly.
- `AnthropicCacheOptions`, `AnthropicCacheStrategy` and `AnthropicCacheTtl` moved from `org.springframework.ai.anthropic.api` to `org.springframework.ai.anthropic`.
- `AnthropicCacheType`, `StreamHelper` and `AnthropicRateLimit` were removed in favour of SDK types such as `CacheControlEphemeral` and `RateLimitException`.
