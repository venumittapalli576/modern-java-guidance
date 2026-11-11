---
id: gradle-configurations
title: 'Use implementation and testImplementation instead of compile and testCompile'
category: build
kind: removed
note: 'The compile, runtime, testCompile and testRuntime configurations were superseded in Gradle 3.4 and removed in Gradle 7 (2021). Drop jcenter() too: JCenter was sunset the same year.'
source: https://docs.gradle.org/current/userguide/upgrading_version_6.html
modern-since: 2017
detect:
  - 'strings build.gradle,build.gradle.kts /^\s*(?:compile|runtime|testCompile|testRuntime)\s*[( ]\s*[''"(\w]/'
  - 'code build.gradle,build.gradle.kts,settings.gradle,settings.gradle.kts /\bjcenter\s*\(\s*\)/'
---

## Before

```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'org.springframework.boot:spring-boot-starter-web'
    runtime 'org.postgresql:postgresql'
    testCompile 'org.springframework.boot:spring-boot-starter-test'
}
```

## After

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    runtimeOnly 'org.postgresql:postgresql'
    testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
}
```

## Notes

| Removed | Use |
|---|---|
| `compile` | `implementation`, or `api` in a library whose consumers need the type |
| `runtime` | `runtimeOnly` |
| `testCompile` | `testImplementation` |
| `testRuntime` | `testRuntimeOnly` |

- Apply plugins with the `plugins { }` block, not `buildscript` plus `apply plugin:`.
