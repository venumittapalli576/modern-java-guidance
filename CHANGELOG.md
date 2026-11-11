# Changelog

## 0.1.0

First release.

- 100 guides in 16 categories: Java language, JDK APIs, concurrency, Jakarta EE, Spring Boot, Spring Web, Spring Security, Spring Data, testing, Jackson 3, Spring Cloud, messaging, Spring Batch, build, runtime and libraries.
- `LegacyScan.java`: a single-file scanner that runs on any JDK 17 or later with no dependencies. Version-aware, with changed-lines mode, text, JSON and GitHub annotation output, and a self-test that proves every guide against its own examples.
- A Claude Code plugin with a `PostToolUse` hook that checks only the lines the agent just wrote.
- An agent skill (`skills/modern-java-guidance`) for any agent that loads the Agent Skills format.
- Examples: a legacy application as agents tend to write it, the same application written for Spring Boot 4 and Java 25, and a JDK-only file of modern forms.
