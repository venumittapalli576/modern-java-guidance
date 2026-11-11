---
id: micrometer-tracing
title: 'Use Micrometer Tracing instead of Spring Cloud Sleuth'
category: spring-cloud
kind: removed
applies: boot>=3.0
note: 'Spring Cloud Sleuth does not support Spring Boot 3. Tracing moved to Micrometer Tracing and Spring Boot''s own observability auto-configuration.'
source: https://github.com/micrometer-metrics/tracing/wiki/Spring-Cloud-Sleuth-3.1-Migration-Guide
modern-since: 2022
detect:
  - 'code *.java /^\s*import\s+org\.springframework\.cloud\.sleuth\./'
  - 'raw pom.xml,build.gradle,build.gradle.kts /spring-cloud-(?:starter-)?sleuth|spring-cloud-starter-zipkin/'
---

## Before

```java
import org.springframework.cloud.sleuth.Tracer;
```

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
```

## After

```java
import io.micrometer.tracing.Tracer;
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

```properties
management.tracing.sampling.probability=1.0
```

## Notes

- Spring Boot 4 has a single `spring-boot-starter-opentelemetry` for the OpenTelemetry route.
- Trace and span ids are still put in the logging MDC as `traceId` and `spanId`.
- Prefer the Observation API (`@Observed`, `ObservationRegistry`) over using `Tracer` directly. One observation yields a span and a timer.
