---
id: spring-ai-mcp-annotations
title: 'Import MCP annotations and transports from Spring AI'
category: spring-ai
kind: removed
applies: boot>=4.0
note: 'Spring AI 2.0 moved the MCP annotations from org.springaicommunity.mcp to org.springframework.ai.mcp.annotation, and the WebFlux and WebMVC transports from the MCP SDK into Spring AI.'
source: https://docs.spring.io/spring-ai/reference/upgrade-notes.html
modern-since: 2026
detect:
  - 'code *.java /\borg\.springaicommunity\.mcp\b|\bio\.modelcontextprotocol\.(?:server|client)\.transport\.(?:WebFlux|WebMvc|WebClient)\w+/'
  - 'raw pom.xml,build.gradle,build.gradle.kts,libs.versions.toml /<artifactId>mcp-annotations<\/artifactId>|org\.springaicommunity:mcp-annotations|io\.modelcontextprotocol\.sdk:mcp-spring-web(?:flux|mvc)/'
---

`@McpTool`, `@McpResource` and `@McpPrompt` started in a community project. Spring AI 2 adopted them, together with the Spring transports that used to ship with the MCP Java SDK.

## Before

```java
import org.springaicommunity.mcp.annotation.McpTool;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
```

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-annotations</artifactId>
</dependency>
```

## After

```java
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcSseServerTransportProvider;
```

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-annotations</artifactId>
</dependency>
```

## Notes

| Before | After |
|---|---|
| `org.springaicommunity.mcp.annotation.*` | `org.springframework.ai.mcp.annotation.*` |
| `org.springaicommunity.mcp.method.*` | `org.springframework.ai.mcp.annotation.method.*` |
| `org.springaicommunity.mcp.provider.*` | `org.springframework.ai.mcp.annotation.provider.*` |
| `io.modelcontextprotocol.server.transport.WebMvc*` | `org.springframework.ai.mcp.server.webmvc.transport.*` |
| `io.modelcontextprotocol.server.transport.WebFlux*` | `org.springframework.ai.mcp.server.webflux.transport.*` |
| `io.modelcontextprotocol.client.transport.WebFlux*`, `WebClientStreamableHttpTransport` | `org.springframework.ai.mcp.client.webflux.transport.*` |

- `mcp-spring-webflux` and `mcp-spring-webmvc` kept their artifact ids but their group is now `org.springframework.ai`.
- `McpSyncClientCustomizer` and `McpAsyncClientCustomizer` were merged into `McpClientCustomizer<B>`.
- Spring AI 2.0 ships MCP Java SDK 2.0. `Tool.inputSchema()` is a `Map<String, Object>`, several constructors became builders, and server-side input validation is on by default.
- Since 2.0.1 a `RuntimeException` thrown from an `@McpTool` method is returned to the model as an error result, the same as `@Tool`.
