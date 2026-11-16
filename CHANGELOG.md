# Changelog

## 0.2.0

AI application code on the JVM.

- 17 new guides in three new categories, for 117 guides in 19 categories:
  - `spring-ai` (11): the 1.0 starter names, `@Tool` and `ToolCallback`, chat memory and conversation ids, explicit tools, the removed Azure OpenAI and `AnthropicApi` types, MCP annotation packages, and renamed configuration keys. Guides for Spring AI 2 apply only on Spring Boot 4.
  - `langchain4j` (3): `ChatModel`, `StreamingChatResponseHandler`, `TokenCountEstimator`.
  - `llm` (3): model ids that have been shut down, the archived community OpenAI client, the deprecated Vertex AI `generativeai` package.
- `retired-model-ids` reports OpenAI, Anthropic and Google model ids that no longer answer, in Java strings and in `application.properties` or `application.yml`. Ids with a platform prefix and deployment names are left alone.
- Examples: `legacy-assistant` (15 findings) and `modern-assistant`, which CI builds against Spring AI 2.0.1 on Spring Boot 4.1.

## 0.1.0

First release.

- 100 guides in 16 categories: Java language, JDK APIs, concurrency, Jakarta EE, Spring Boot, Spring Web, Spring Security, Spring Data, testing, Jackson 3, Spring Cloud, messaging, Spring Batch, build, runtime and libraries.
- `LegacyScan.java`: a single-file scanner that runs on any JDK 17 or later with no dependencies. Version-aware, with changed-lines mode, text, JSON and GitHub annotation output, and a self-test that proves every guide against its own examples.
- A Claude Code plugin with a `PostToolUse` hook that checks only the lines the agent just wrote.
- An agent skill (`skills/modern-java-guidance`) for any agent that loads the Agent Skills format.
- Examples: a legacy application as agents tend to write it, the same application written for Spring Boot 4 and Java 25, and a JDK-only file of modern forms.
