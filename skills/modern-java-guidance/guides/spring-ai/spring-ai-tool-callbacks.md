---
id: spring-ai-tool-callbacks
title: 'Use @Tool and ToolCallback instead of FunctionCallback'
category: spring-ai
kind: deprecated
removed: boot>=4.0
note: 'Spring AI 1.0.0-M6 replaced FunctionCallback, FunctionCallingOptions and ChatClient.functions() with ToolCallback, ToolCallingChatOptions and tools(). The old names are not in Spring AI 2.'
source: https://docs.spring.io/spring-ai/reference/1.1/api/tools-migration.html
modern-since: 2025
when-file: 'org\.springframework\.ai\.'
detect:
  - 'code *.java /\bFunctionCallback(?:Wrapper)?\b|\bFunctionCallingOptions\b|\.defaultFunctions\s*\(|\.functionCallbacks\s*\(|\.functions\s*\(/'
---

"Function calling" became "tool calling" before Spring AI 1.0 shipped. The API an agent remembers from 2024 samples is the one that was replaced.

## Before

```java
import org.springframework.ai.model.function.FunctionCallback;

FunctionCallback weather = FunctionCallback.builder()
        .function("currentWeather", new WeatherService())
        .description("Get the weather in a location")
        .inputType(WeatherRequest.class)
        .build();

String answer = chatClient.prompt()
        .user("What is the weather in Detroit?")
        .functions(weather)
        .call()
        .content();
```

## After

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

class WeatherTools {

    @Tool(description = "Get the current weather in a location")
    String currentWeather(@ToolParam(description = "City and state") String location) {
        return weatherService.lookup(location);
    }
}

String answer = chatClient.prompt()
        .user("What is the weather in Detroit?")
        .tools(new WeatherTools())
        .call()
        .content();
```

## Notes

| Before | After |
|---|---|
| `FunctionCallback` | `ToolCallback` |
| `FunctionCallback.builder().function(name, fn)` | `FunctionToolCallback.builder(name, fn)` |
| `FunctionCallingOptions` | `ToolCallingChatOptions` |
| `ChatClient.Builder.defaultFunctions(...)` | `defaultTools(...)` |
| `chatClient.prompt().functions(...)` | `tools(...)` |

- A method annotated with `@Tool` is the shortest form. Reach for `FunctionToolCallback` when the tool is a `Function` you already have.
- On Spring AI 2 also read `spring-ai-explicit-tools`: tool names are no longer resolved from the application context.
