---
id: maven-compiler-release
title: 'Set the Java level with maven.compiler.release, not source and target'
category: build
kind: superseded
applies: java>=9
note: 'The compiler''s --release flag (Java 9) checks API usage as well as syntax. With only source and target, calls to newer JDK APIs compile and then fail at run time on the older JVM.'
source: https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-release.html
modern-since: 2017
detect:
  - 'code pom.xml /<maven\.compiler\.(?:source|target)>|<(?:source|target)>\s*[\d.]+\s*<\/(?:source|target)>/'
---

## Before

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>
```

## After

```xml
<properties>
    <maven.compiler.release>21</maven.compiler.release>
</properties>
```

## Notes

- With the Spring Boot parent POM, set only `<java.version>21</java.version>`. The parent wires it to the compiler.
- In Gradle the equivalent is a toolchain: `java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }`.
