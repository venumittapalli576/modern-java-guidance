---
id: spring-ai-openai-modules
title: 'Use spring-ai-openai for OpenAI and Azure OpenAI'
category: spring-ai
kind: removed
applies: boot>=4.0
note: 'Spring AI 2.0 runs every OpenAI model on the official openai-java SDK and removed spring-ai-azure-openai and spring-ai-openai-sdk. Use spring-ai-openai and drop the Azure prefix or Sdk suffix.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2026
detect:
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /spring-ai-(?:starter-model-|autoconfigure-model-)?(?:azure-openai|openai-sdk)(?![\w-])/'
  - 'code *.java /\bAzureOpenAi(?:ChatModel|ChatOptions|EmbeddingModel|EmbeddingOptions|ImageModel|ImageOptions|AudioTranscriptionModel)\b|\bOpenAiSdk[A-Z]\w+\b/'
---

Spring AI 1 had three ways to reach OpenAI models: its own HTTP client, a module on the official SDK, and a separate Azure module. Spring AI 2 has one.

## Before

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-azure-openai</artifactId>
</dependency>
```

```java
AzureOpenAiChatOptions options = AzureOpenAiChatOptions.builder()
        .deploymentName(deployment)
        .build();
```

## After

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

```java
OpenAiChatOptions options = OpenAiChatOptions.builder()
        .model(deployment)
        .build();
```

## Notes

- Configuration stays under `spring.ai.openai.*`.
- `extraBody` maps to the SDK's `additionalBodyProperties`.
- Since 2.0.1 OpenAI strict tool schemas are opt-in: `OpenAiChatOptions.builder().strict(true)`.
- `spring-ai-oci-genai` left the project as well. It lives in Oracle's `spring-cloud-oracle` repository now.
