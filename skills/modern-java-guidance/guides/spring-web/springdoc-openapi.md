---
id: springdoc-openapi
title: 'Use springdoc-openapi instead of Springfox'
category: spring-web
kind: hazard
removed: boot>=3.0
note: 'Springfox has had no release since 2020, breaks on Spring Boot 2.6+ path matching and does not support Jakarta EE. springdoc-openapi is the maintained OpenAPI 3 integration.'
source: https://springdoc.org/#migrating-from-springfox
modern-since: 2019
limit: 1
detect:
  - 'code *.java /@EnableSwagger2\b|^\s*import\s+springfox\./'
  - 'code *.java /^\s*import\s+io\.swagger\.annotations\./'
  - 'raw pom.xml,build.gradle,build.gradle.kts /io\.springfox|springfox-(?:swagger2|swagger-ui|boot-starter)/'
---

Springfox and its `Docket` bean are what most tutorials still show. On Spring Boot 3 it cannot start, because it depends on `javax.servlet`. springdoc needs no configuration class: add the starter and the UI is served at `/swagger-ui.html`.

## Before

```java
import io.swagger.annotations.ApiOperation;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select().build();
    }
}
```

```xml
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-boot-starter</artifactId>
    <version>3.0.0</version>
</dependency>
```

## After

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Orders")
public class OrderController {

    @Operation(summary = "Find an order by id")
    @GetMapping("/orders/{id}")
    public Order find(@PathVariable long id) {
        return orders.find(id);
    }
}
```

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>${springdoc.version}</version>
</dependency>
```

## Notes

- springdoc 2.x targets Spring Boot 3 and springdoc 3.x targets Spring Boot 4. Pick the line that matches your Boot generation.
- Annotation mapping: `@Api` to `@Tag`, `@ApiOperation` to `@Operation`, `@ApiParam` to `@Parameter`, `@ApiModelProperty` to `@Schema`, `@ApiResponse(code = 404, message = "...")` to `@ApiResponse(responseCode = "404", description = "...")`.
- Global metadata goes in an `OpenAPI` bean, not a `Docket`.
