---
id: langchain4j-chat-model
title: 'Use ChatModel and chat() in LangChain4j 1.x'
category: langchain4j
kind: removed
note: 'LangChain4j 1.0.0-rc1 renamed ChatLanguageModel to ChatModel and StreamingChatLanguageModel to StreamingChatModel. The generate() methods had already been replaced by chat().'
source: https://github.com/langchain4j/langchain4j/releases/tag/1.0.0-rc1
modern-since: 2025
when-file: 'dev\.langchain4j\.'
detect:
  - 'code *.java /\b(?:Streaming)?ChatLanguageModel\b/'
---

Nearly every LangChain4j example written before May 2025 declares a `ChatLanguageModel` and calls `generate`. Neither exists in a 1.x release.

## Before

```java
import dev.langchain4j.model.chat.ChatLanguageModel;

ChatLanguageModel model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .modelName(modelName)
        .build();

String answer = model.generate("Summarise this ticket: " + ticket);
```

## After

```java
import dev.langchain4j.model.chat.ChatModel;

ChatModel model = OpenAiChatModel.builder()
        .apiKey(apiKey)
        .modelName(modelName)
        .build();

String answer = model.chat("Summarise this ticket: " + ticket);
```

## Notes

- For anything beyond a single string, build a `ChatRequest` and read the `ChatResponse`: `model.chat(ChatRequest.builder().messages(messages).build()).aiMessage()`.
- Collections returned by the API are never `null` since 1.0.0-rc1. An absent list of tool requests is an empty list.
- The project published an OpenRewrite recipe for this step, `dev.langchain4j.UpgradeToRc1AndBeta4`.
- Import `dev.langchain4j:langchain4j-bom` so the core modules and the integrations stay on matching versions.
