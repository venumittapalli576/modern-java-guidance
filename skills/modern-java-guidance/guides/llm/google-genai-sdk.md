---
id: google-genai-sdk
title: 'Use the Google Gen AI SDK instead of the Vertex AI generativeai package'
category: llm
kind: deprecated
note: 'Google deprecated the generative AI module of the Vertex AI SDK on 2025-06-24 and stopped shipping it in releases after 2026-06-24. Use the Google Gen AI SDK, com.google.genai:google-genai.'
source: https://docs.cloud.google.com/vertex-ai/generative-ai/docs/deprecations/genai-vertexai-sdk
modern-since: 2025
detect:
  - 'code *.java /\bcom\.google\.cloud\.vertexai\.generativeai\b/'
---

`GenerativeModel` and `ResponseHandler` from `google-cloud-vertexai` were how Java code called Gemini on Vertex AI. One SDK now serves both Vertex AI and the Gemini Developer API, and new features such as context caching only exist there.

## Before

```java
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

try (VertexAI vertexAi = new VertexAI(projectId, location)) {
    GenerativeModel model = new GenerativeModel(modelName, vertexAi);
    String text = ResponseHandler.getText(model.generateContent(prompt));
}
```

## After

```java
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

Client client = Client.builder()
        .vertexAI(true)
        .project(projectId)
        .location(location)
        .build();

GenerateContentResponse response = client.models.generateContent(modelName, prompt, null);
String text = response.text();
```

## Notes

- The dependency changes from `com.google.cloud:google-cloud-vertexai` to `com.google.genai:google-genai`.
- Existing versions of the old artifact still resolve, which is why this is reported as deprecated and not removed. Upgrading the old artifact past June 2026 is what breaks the build.
- The rest of `google-cloud-vertexai` (endpoints, prediction, the `com.google.cloud.vertexai.api` clients) is not affected.
