---
id: openai-official-java-sdk
title: 'Use the official OpenAI Java SDK, not the archived community client'
category: llm
kind: deprecated
note: 'com.theokanning.openai-gpt3-java was archived on 2024-06-06. It predates the Responses API and every current model. OpenAI publishes an official SDK as com.openai:openai-java.'
source: https://github.com/openai/openai-java
modern-since: 2025
detect:
  - 'code *.java /\bcom\.theokanning\.openai\b/'
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /com\.theokanning\.openai-gpt3-java/'
---

For years the Java client everyone used for OpenAI was a community project, and OpenAI's own documentation linked to it. That is the client in an agent's memory. It has been read-only since June 2024.

## Before

```java
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

OpenAiService service = new OpenAiService(apiKey);
ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model(model)
        .messages(List.of(new ChatMessage("user", question)))
        .build();
String answer = service.createChatCompletion(request).getChoices().get(0).getMessage().getContent();
```

```xml
<dependency>
    <groupId>com.theokanning.openai-gpt3-java</groupId>
    <artifactId>service</artifactId>
    <version>0.18.2</version>
</dependency>
```

## After

```java
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

OpenAIClient client = OpenAIOkHttpClient.fromEnv();

ResponseCreateParams params = ResponseCreateParams.builder()
        .model(model)
        .input(question)
        .build();
Response response = client.responses().create(params);
```

```xml
<dependency>
    <groupId>com.openai</groupId>
    <artifactId>openai-java</artifactId>
    <version>${openai-java.version}</version>
</dependency>
```

## Notes

- `OpenAIOkHttpClient.fromEnv()` reads `OPENAI_API_KEY`. Create one client and share it.
- In a Spring application you usually want Spring AI's `ChatClient` or LangChain4j instead of the raw SDK. Spring AI 2 uses this SDK underneath.
- The Assistants API that the old client wrapped was shut down on 2026-08-26.
