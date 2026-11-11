---
id: docker-base-image
title: 'Use a maintained JDK base image instead of openjdk'
category: runtime
kind: deprecated
note: 'The official openjdk image on Docker Hub is deprecated and no longer publishes builds of released Java versions. java:8 and adoptopenjdk are long gone. Use eclipse-temurin or another vendor image.'
source: https://hub.docker.com/_/openjdk
modern-since: 2022
detect:
  - 'code Dockerfile*,*.dockerfile,docker-compose*.yml,docker-compose*.yaml,compose*.yml,compose*.yaml /(?:^\s*FROM\s+(?:--platform=\S+\s+)?|\bimage:\s*["'']?)(?:openjdk|adoptopenjdk(?:\/[\w-]+)?|java):/i'
---

`FROM openjdk:8-jdk-alpine` opens nearly every Spring Boot Dockerfile ever published. That image stopped receiving security updates when the project was deprecated.

## Before

```dockerfile
FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## After

```dockerfile
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/app.jar app.jar
USER 1000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Notes

- Use a JRE image to run and a JDK image only to build. Match the tag to the Java version of the project.
- Other maintained images: `amazoncorretto`, `azul/zulu-openjdk`, `bellsoft/liberica-openjdk-alpine`, `mcr.microsoft.com/openjdk/jdk`.
- Spring Boot can build the image with no Dockerfile at all: `mvn spring-boot:build-image` or `gradle bootBuildImage`.
- Do not run as root. `USER 1000`, or the image's own non-root user, is enough for most applications.
