---
id: langchain4j-token-count-estimator
title: 'Use TokenCountEstimator instead of Tokenizer'
category: langchain4j
kind: removed
note: 'LangChain4j 1.0.0-rc1 renamed Tokenizer to TokenCountEstimator, and OpenAiTokenizer to OpenAiTokenCountEstimator.'
source: https://github.com/langchain4j/langchain4j/releases/tag/1.0.0-rc1
modern-since: 2025
detect:
  - 'code *.java /\bimport\s+dev\.langchain4j\.model\.Tokenizer\b|\b(?:OpenAi|AzureOpenAi)Tokenizer\b/'
---

Document splitters and token-window chat memories take a token counter. Its type was renamed for 1.0.

## Before

```java
import dev.langchain4j.model.Tokenizer;
import dev.langchain4j.model.openai.OpenAiTokenizer;

Tokenizer tokenizer = new OpenAiTokenizer(modelName);
DocumentSplitter splitter = DocumentSplitters.recursive(500, 50, tokenizer);
```

## After

```java
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;

TokenCountEstimator estimator = new OpenAiTokenCountEstimator(modelName);
DocumentSplitter splitter = DocumentSplitters.recursive(500, 50, estimator);
```

## Notes

- The name is honest about what it does: for most providers the count is an estimate made locally, not the provider's own number.
- The streaming Azure OpenAI model lost its estimator parameter in 1.2.0.
