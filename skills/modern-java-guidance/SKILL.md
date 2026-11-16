---
name: modern-java-guidance
description: Keeps Java and Spring Boot code current. Use whenever writing, editing, reviewing or migrating Java source, Spring Boot or Spring Security configuration, JPA entities, tests, Maven or Gradle builds, application.properties or application.yml, Dockerfiles or JVM flags for a Java application. Covers javax to jakarta, Spring Boot 3 and 4, Spring Security 6 and 7, Jackson 3, JUnit Jupiter, Java 17 to 25 language features, virtual threads, and removed JDK APIs and JVM options. Also covers AI application code on the JVM: Spring AI 1 and 2, LangChain4j 1.x, the OpenAI and Google Gen AI Java SDKs, MCP annotations, and LLM model ids that have been shut down. Includes a scanner (JDK only, no install) that finds legacy patterns in the lines you changed.
license: Apache-2.0
compatibility: The bundled scanner needs a JDK 17 or later on PATH. The guides need nothing.
metadata:
  version: "0.2.0"
  homepage: https://github.com/venumittapalli576/modern-java-guidance
---

# Modern Java guidance

Most Java ever published was written for Java 8 and Spring Boot 1 or 2. Left alone, a model reproduces that majority: `javax.*` imports, `WebSecurityConfigurerAdapter`, `RestTemplate`, `@MockBean`, JUnit 4, `new Date()`, and JVM flags that stop a current JVM from starting. Some of it is merely dated. Much of it no longer compiles.

This skill is 117 short guides, each a legacy pattern with its current replacement, the version where the replacement became available, and the version where the old form was deprecated or removed. A bundled scanner enforces the same guides mechanically.

## How to work

1. **Find the project's versions first.** Read `pom.xml` or `build.gradle` for the Java level and the Spring Boot version before writing code. Write for those versions. Do not use a language feature or API newer than the project's Java level, and do not assume Spring Boot 4 in a Spring Boot 3 project.
2. **For a new project with no constraints**, target Java 25 (the current LTS) and the current Spring Boot 4 release.
3. **Write the current form the first time.** The table below covers the patterns that go wrong most often.
4. **Scan what you changed.** After writing or editing Java, build, Docker or Spring configuration files, run the scanner and fix what it reports before moving on:

   ```sh
   java scripts/LegacyScan.java scan --changed          # lines changed since the last commit, plus new files
   java scripts/LegacyScan.java scan src/main/java/com/acme/SecurityConfig.java
   ```

   The path is relative to this skill's directory. The scanner reads the project's versions from the build, so it only reports what applies to this project.
5. **Read the guide when a finding is not obvious.** Each one is at `guides/<category>/<id>.md` with a before and after. `java scripts/LegacyScan.java explain <id>` prints it.
6. **Stay in scope.** Modernize the code you were asked to write or change. If the scanner shows legacy patterns elsewhere in the project, tell the user. Do not rewrite files you were not asked to touch.

If a legacy form is deliberate, for example a vendor API that needs a `java.util.Date`, keep it and mark the line: `// modern-java:ignore java-time the billing SDK takes a Date`.

## Statuses

| Status | Meaning | What to do |
|---|---|---|
| removed | Gone on this project's versions. Does not compile, does not start, or is silently ignored | Always fix |
| deprecated | Still works, officially deprecated, often scheduled for removal | Fix in code you write |
| hazard | Compiles and runs, but is a known source of bugs or is not what it appears to be | Fix, or justify with an ignore comment |
| superseded | Works and is not deprecated, but there is a clearly better standard replacement | Use the modern form in new code |

## The short list

Check the "from" column against the project before applying a row.

| Do not write | Write | From |
|---|---|---|
| `import javax.persistence.*`, `javax.servlet.*`, `javax.validation.*` | `jakarta.persistence.*`, `jakarta.servlet.*`, `jakarta.validation.*` | Boot 3.0 |
| `extends WebSecurityConfigurerAdapter` | A `SecurityFilterChain` `@Bean` | Boot 2.7, removed in 3.0 |
| `.authorizeRequests()`, `.antMatchers(...)` | `.authorizeHttpRequests(...)`, `.requestMatchers(...)` | Boot 2.7. `antMatchers` removed in 3.0, `authorizeRequests` in 4.0 |
| `http.csrf().disable().and()...` | `http.csrf(csrf -> csrf.disable())`, every configurer takes a lambda | Boot 2.7, removed in 4.0 |
| `@EnableGlobalMethodSecurity(prePostEnabled = true)` | `@EnableMethodSecurity` | Boot 2.7 |
| `new RestTemplate()` | `RestClient`, built from an injected `RestClient.Builder` | Boot 3.2, `RestTemplate` deprecated in 4.2 |
| `webClient...block()` in an MVC app | `RestClient` | Boot 3.2 |
| `@FeignClient` in new code | An interface with `@HttpExchange` methods | Boot 3.0 |
| `@Autowired` on a field | Constructor injection into `final` fields | Always |
| `@MockBean`, `@SpyBean` | `@MockitoBean`, `@MockitoSpyBean` | Boot 3.4, removed in 4.0 |
| `import org.junit.Test`, `@RunWith(SpringRunner.class)` | JUnit Jupiter: `org.junit.jupiter.api.Test`, no runner | Always |
| `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest` | `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` and `spring-boot-starter-webmvc-test` | Boot 4.0 |
| `TestRestTemplate` | `RestTestClient` with `@AutoConfigureRestTestClient` | Boot 4.0 |
| `@DynamicPropertySource` for a Testcontainers database | `@ServiceConnection` on the container | Boot 3.1 |
| `import com.fasterxml.jackson.databind.ObjectMapper` | `tools.jackson.databind.json.JsonMapper`. Annotations stay in `com.fasterxml.jackson.annotation` | Boot 4.0 |
| `new ObjectMapper()` inside a bean | Inject the mapper Spring Boot configured | Always |
| `spring-boot-starter-web`, `-aop`, `-oauth2-resource-server` | `spring-boot-starter-webmvc`, `-aspectj`, `-security-oauth2-resource-server` | Boot 4.0 |
| `spring.redis.*`, `spring.data.mongodb.uri`, `management.metrics.export.*` | `spring.data.redis.*`, `spring.mongodb.uri`, `management.<product>.metrics.export.*` | Boot 3.0 and 4.0 |
| `hibernate.dialect=...MySQL8Dialect` | Nothing. Hibernate detects the dialect | Boot 3.0, removed in 4.0 |
| `@Type(type = "jsonb")`, `@Where(clause = ...)` | `@JdbcTypeCode(SqlTypes.JSON)`, `@SQLRestriction(...)` | Boot 3.0 and 3.2 |
| Lombok `@Data` on an `@Entity` | `@Getter`, `@Setter` and an id-based `equals` | Always |
| Springfox, `@EnableSwagger2`, `Docket` | springdoc-openapi | Boot 3.0 |
| `@EnableZuulProxy`, `@HystrixCommand`, Ribbon | Spring Cloud Gateway, Resilience4j, Spring Cloud LoadBalancer | Boot 2.4 |
| Spring Cloud Sleuth | Micrometer Tracing | Boot 3.0 |
| `JobBuilderFactory`, `StepBuilderFactory` | `new JobBuilder(name, jobRepository)`, `new StepBuilder(...)` | Boot 3.0 |
| JJWT `setSigningKey`, `parseClaimsJws`, `SignatureAlgorithm.HS256` | JJWT 0.12: `verifyWith(key)`, `parseSignedClaims`, `signWith(key)` | JJWT 0.12 |
| `com.amazonaws.*` (AWS SDK 1.x) | `software.amazon.awssdk.*` (2.x) | Always |
| `spring-ai-openai-spring-boot-starter` | `spring-ai-starter-model-openai`, with the `spring-ai-bom` | Spring AI 1.0 |
| `FunctionCallback`, `.functions(...)`, `.defaultFunctions(...)` | `@Tool` methods passed with `.tools(...)` or `.defaultTools(...)` | Spring AI 1.0, removed in 2.0 |
| `PromptChatMemoryAdvisor`, an advisor built with a fixed `conversationId(...)` | `MessageChatMemoryAdvisor`, and `ChatMemory.CONVERSATION_ID` on every call | Spring AI 2.0 |
| `.toolNames("...")`, `spring.ai.openai.chat.options.model` | Explicit `.tools(...)`, `spring.ai.openai.chat.model` | Spring AI 2.0 |
| `ChatLanguageModel`, `model.generate(...)`, `StreamingResponseHandler<AiMessage>` | `ChatModel`, `model.chat(...)`, `StreamingChatResponseHandler` | LangChain4j 1.0 |
| `com.theokanning.openai`, `com.google.cloud.vertexai.generativeai` | `com.openai:openai-java`, `com.google.genai:google-genai` | Always |
| `"gpt-4"`, `"gpt-3.5-turbo"`, `"claude-3-5-sonnet-20241022"`, `"gemini-1.5-pro"` | No model id from memory. Look up the provider's current list, or read the id from configuration | All shut down |
| `new Date()`, `Calendar`, `SimpleDateFormat` | `java.time`: `Instant`, `LocalDate`, `DateTimeFormatter` | Java 8 |
| Lombok `@Value` class for a DTO | `record` | Java 16 |
| `if (o instanceof Foo) { Foo f = (Foo) o;` | `if (o instanceof Foo f)` | Java 16 |
| `switch` with `case X:` and `break` | `switch` with `case X ->`, as an expression | Java 14 |
| `else if (x instanceof ...)` chains | `switch` with type patterns over a sealed type | Java 21 |
| `.collect(Collectors.toList())` | `.toList()` | Java 16 |
| `"line one\n" + "line two"` | A text block `"""` | Java 15 |
| `Executors.newFixedThreadPool(n)` for blocking I/O | `Executors.newVirtualThreadPerTaskExecutor()`, or `spring.threads.virtual.enabled=true` | Java 21 |
| `DatatypeConverter`, `sun.misc.BASE64Encoder` | `java.util.Base64`, `HexFormat` | Java 11, removed |
| `new URL(s)`, `new Locale("en")`, `new Integer(1)` | `URI.create(s).toURL()`, `Locale.of("en")`, `Integer.valueOf(1)` | Java 20, 19, 9 |
| `FROM openjdk:8-jdk-alpine` | `FROM eclipse-temurin:25-jre` | Always |
| `-XX:MaxPermSize`, `-XX:+UseConcMarkSweepGC`, `-XX:+PrintGCDateStamps` | Delete them. `-Xlog:gc*` for GC logs, `-XX:MaxRAMPercentage` for heap | Java 9 to 17, the JVM will not start |

## Things that look modern but are not

Newer material gets these wrong, so they turn up in code that otherwise looks up to date.

- **`StructuredTaskScope` is a preview API**, still in its seventh preview in Java 27. It needs `--enable-preview`, and its shape changed in Java 25, so `new StructuredTaskScope.ShutdownOnFailure()` no longer compiles. Use `Executors.newVirtualThreadPerTaskExecutor()` in try-with-resources unless the project has opted in to previews.
- **String templates (`STR."..."`) do not exist.** They were a preview in Java 21 and 22 and were withdrawn in 23.
- **Do not rewrite `synchronized` to `ReentrantLock` "to avoid pinning virtual threads".** That was true on Java 21 to 23. Java 24 fixed it (JEP 491).
- **Primitive type patterns and lazy constants are previews** through Java 27. Scoped values are final from Java 25. Unnamed variables (`_`) are final from Java 22.
- **A model id you remember is probably retired.** OpenAI shut down `gpt-4`, `gpt-4-turbo` and `gpt-3.5-turbo` on 2026-10-23. Claude 3.x, Claude 4.0 and 4.1, and Gemini 1.x and 2.0 ids are gone as well. The code compiles and every real request fails. Never write a model id from memory: look it up, or leave it in configuration and ask. See `guides/llm/retired-model-ids.md`.
- **Spring AI 1 is for Spring Boot 3, Spring AI 2 is for Spring Boot 4.** Most Spring AI samples predate 1.0 and use starter names, `FunctionCallback` and option keys that no release has.
- **In Spring AI 2, `ChatModel.call` does not run tools.** The tool loop lives in `ChatClient`. A direct `ChatModel` call returns the raw tool-call request.
- **Spring Boot 4 still runs on Java 17.** Do not raise a project's Java level as a side effect of a Boot upgrade.
- **Jackson annotations did not move.** On Boot 4, `@JsonProperty` is still `com.fasterxml.jackson.annotation.JsonProperty`. Only databind, core and the modules moved to `tools.jackson`.
- **JDK `javax` packages did not move.** `javax.sql`, `javax.crypto`, `javax.net.ssl`, `javax.naming`, `javax.xml.parsers` and `javax.annotation.processing` are correct as they are.

## Scanner reference

```sh
java scripts/LegacyScan.java scan [paths]            # default: current directory
java scripts/LegacyScan.java scan --changed [ref]    # only lines changed since a git ref (default HEAD)
java scripts/LegacyScan.java scan --min deprecated   # hide the superseded tier
java scripts/LegacyScan.java scan --boot 4.0 --java 25   # what would break after an upgrade
java scripts/LegacyScan.java explain <id>            # print one guide
java scripts/LegacyScan.java search <words>          # find a guide by keyword
java scripts/LegacyScan.java list [category]         # every guide
```

The scanner is pattern-based. It does not compile the code or resolve types, so treat a finding as a strong hint and read the line. When the build does not reveal its versions, the scanner says what it assumed. A `.modern-java` file at the project root pins them (`java=21`, `boot=3.5` or `boot=none`).

## All guides

[INDEX.md](INDEX.md) lists every guide by category with its status and the versions it applies to.

| Category | Covers |
|---|---|
| `language` | Records, pattern matching, switch, text blocks, collections, Optional |
| `jdk-apis` | java.time, HTTP client, removed and deprecated JDK APIs |
| `concurrency` | Virtual threads, scoped values, structured concurrency status |
| `jakarta` | The javax to jakarta namespace change |
| `spring-boot` | Injection, configuration properties, starters, renamed keys |
| `spring-web` | RestClient, HTTP service clients, MVC, OpenAPI, problem details |
| `spring-security` | SecurityFilterChain, the lambda DSL, request matchers, OAuth2 |
| `spring-data` | Hibernate 6 and 7, JPA, Elasticsearch, Lombok on entities |
| `spring-testing` | JUnit Jupiter, bean overrides, test slices, Testcontainers |
| `jackson` | Jackson 3 on Spring Boot 4 |
| `spring-cloud`, `messaging`, `spring-batch` | Netflix OSS replacements, tracing, Stream, Kafka, Batch 5 |
| `build` | Maven and Gradle settings, relocated artifacts |
| `runtime` | JVM flags and container base images |
| `libraries` | JJWT, AWS SDK, logging, Commons, Guava |
| `spring-ai` | Spring AI 1 and 2: starters, tools, chat memory, MCP, configuration keys |
| `langchain4j` | LangChain4j 1.x: ChatModel, streaming, token counting |
| `llm` | Retired model ids, the official OpenAI and Google Gen AI SDKs |
