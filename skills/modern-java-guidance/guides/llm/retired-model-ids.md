---
id: retired-model-ids
title: 'Do not hard-code a retired model id'
category: llm
kind: removed
note: 'OpenAI shut down gpt-4, gpt-4-turbo and gpt-3.5-turbo on 2026-10-23. Claude 3.x, Claude 4.0 and 4.1, and Gemini 1.x and 2.0 ids are retired as well. A request that names a retired id fails.'
source: https://developers.openai.com/api/docs/deprecations
modern-since: 2026
detect:
  - 'strings *.java /^(?!.*(?i:deployment)).*?(?<![\w.\/-])(?:gpt-3\.5-turbo[\w.-]*|gpt-4(?:-0613|-32k(?:-0314|-0613)?|-turbo(?:-preview)?|-1106-preview|-0125-preview|-vision-preview|-1106-vision-preview)?|gpt-4\.5-preview|text-davinci-00[123]|code-davinci-002|o1-preview|o1-mini|o3-mini|chatgpt-4o-latest|dall-e-[23])(?![\w.@-])/'
  - 'strings *.java /^(?!.*(?i:deployment)).*?(?<![\w.\/-])claude-(?:2\.[01]|instant-1\.2|3-(?:opus|sonnet|haiku)-2024\d{4}|3-5-(?:sonnet|haiku)-2024\d{4}|3-7-sonnet-20250219|(?:sonnet|opus)-4-20250514|opus-4-1-20250805|3-(?:opus|5-sonnet|5-haiku|7-sonnet)-latest)(?![\w.@-])/'
  - 'strings *.java /^(?!.*(?i:deployment)).*?(?<![\w.-])(?:gemini-pro(?:-vision)?|gemini-1\.0-pro[\w.-]*|gemini-1\.5-(?:pro|flash)[\w.-]*|gemini-2\.0-flash(?:-lite)?(?:-001)?|(?:text|chat)-bison(?:-32k)?(?:@\d+)?|textembedding-gecko(?:@\d+)?)(?![\w.@-])/'
  - 'code application*.properties,application*.yml,application*.yaml /^(?!.*(?i:deployment)).*?[=:\s](?:gpt-3\.5-turbo[\w.-]*|gpt-4(?:-0613|-32k(?:-0314|-0613)?|-turbo(?:-preview)?|-1106-preview|-0125-preview|-vision-preview)?|gpt-4\.5-preview|o1-preview|o1-mini|o3-mini|dall-e-[23]|claude-(?:2\.[01]|3-(?:opus|sonnet|haiku)-2024\d{4}|3-5-(?:sonnet|haiku)-2024\d{4}|3-7-sonnet-20250219|(?:sonnet|opus)-4-20250514|opus-4-1-20250805|3-(?:opus|5-sonnet|5-haiku|7-sonnet)-latest)|gemini-pro(?:-vision)?|gemini-1\.0-pro[\w.-]*|gemini-1\.5-(?:pro|flash)[\w.-]*|gemini-2\.0-flash(?:-lite)?(?:-001)?)\s*$/'
---

A model id is the most perishable string in an AI application, and the one a coding agent is most likely to write from memory. Whatever was current when the agent was trained is, by the time you read its code, often retired. The code compiles, the tests with a mocked client pass, and the first real request returns an error.

## Before

```java
OpenAiChatOptions options = OpenAiChatOptions.builder()
        .model("gpt-4")
        .build();

AnthropicChatOptions fallback = AnthropicChatOptions.builder()
        .model("claude-3-5-sonnet-20241022")
        .build();

GenerateContentResponse response = client.models.generateContent("gemini-1.5-pro", prompt, null);
```

```properties
spring.ai.openai.chat.model=gpt-3.5-turbo
```

## After

```java
@ConfigurationProperties("assistant")
record AssistantProperties(String chatModel, String fallbackModel) {
}
```

```properties
# Set per environment. Check the provider's deprecations page whenever this changes.
assistant.chat-model=${ASSISTANT_CHAT_MODEL}
assistant.fallback-model=${ASSISTANT_FALLBACK_MODEL}
```

## Notes

If you are an agent: do not write a model id from memory. Look up the provider's current model list, or leave the id in configuration and ask which model to use.

Retired ids this guide recognises, checked on 2026-11-16 against each provider's own page:

| Provider | Retired ids | Shut down |
|---|---|---|
| OpenAI | `gpt-4`, `gpt-4-0613`, `gpt-4-turbo`, `gpt-4-1106-preview`, `gpt-3.5-turbo`, `gpt-3.5-turbo-0125`, `o3-mini` | 2026-10-23 |
| OpenAI | `gpt-3.5-turbo-1106`, `gpt-3.5-turbo-instruct` | 2026-09-28 |
| OpenAI | `dall-e-2`, `dall-e-3` | 2026-05-12 |
| OpenAI | `gpt-4-turbo-preview`, `gpt-4-0125-preview` | 2026-03-26 |
| OpenAI | `chatgpt-4o-latest` | 2026-02-17 |
| OpenAI | `o1-mini`, `o1-preview`, `gpt-4.5-preview`, `gpt-4-32k` | 2025 |
| OpenAI | `gpt-4-vision-preview`, `gpt-3.5-turbo-0613`, `text-davinci-003` | 2024 |
| Anthropic | `claude-opus-4-1-20250805` | 2026-08-05 |
| Anthropic | `claude-sonnet-4-20250514`, `claude-opus-4-20250514` | 2026-06-15 |
| Anthropic | `claude-3-haiku-20240307` | 2026-04-20 |
| Anthropic | `claude-3-7-sonnet-20250219`, `claude-3-5-haiku-20241022` | 2026-02-19 |
| Anthropic | `claude-3-opus-20240229` | 2026-01-05 |
| Anthropic | `claude-3-5-sonnet-20240620`, `claude-3-5-sonnet-20241022` | 2025-10-28 |
| Anthropic | `claude-3-sonnet-20240229`, `claude-2.1`, `claude-2.0` | 2025-07-21 |
| Google | `gemini-2.0-flash`, `gemini-2.0-flash-lite` and their `-001` versions | 2026-06-01 |
| Google | `gemini-1.5-pro`, `gemini-1.5-flash` | 2025-09-24 |
| Google | `gemini-1.0-pro`, `gemini-pro`, `text-bison`, `chat-bison` | 2025-04-21 |

- Sources: [OpenAI deprecations](https://developers.openai.com/api/docs/deprecations), [Anthropic model deprecations](https://platform.claude.com/docs/en/about-claude/model-deprecations), [Vertex AI model versions](https://docs.cloud.google.com/vertex-ai/generative-ai/docs/learn/model-versions).
- The dates are for each provider's own API. The same model on Amazon Bedrock, Azure or another gateway follows that platform's schedule, so ids with a platform prefix (`anthropic.claude-...`, `openai/gpt-...`) and lines that mention a deployment are not flagged.
- The OpenAI Assistants API was shut down on 2026-08-26. Its replacement is the Responses API.
- This list goes stale by design. A pull request that adds a newly retired id, with a link to the provider's page, is the most useful contribution this project can get.
