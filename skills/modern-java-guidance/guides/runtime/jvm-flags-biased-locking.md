---
id: jvm-flags-biased-locking
title: 'Remove -XX:UseBiasedLocking'
category: runtime
kind: deprecated
applies: java>=15
removed: java>=21
note: 'Biased locking was disabled and deprecated in Java 15 (JEP 374). On JDK 21 the option is gone and -XX:+UseBiasedLocking or -XX:-UseBiasedLocking stops the JVM from starting.'
source: https://openjdk.org/jeps/374
modern-since: 2020
detect:
  - 'code Dockerfile*,*.dockerfile,*.sh,*.yml,*.yaml,*.properties,*.conf,*.env,jvm.config,jvm.options,pom.xml,build.gradle,build.gradle.kts,Procfile,*.service,Makefile,*.bat,*.cmd /-XX:[+-]UseBiasedLocking\b/'
---

`-XX:-UseBiasedLocking` was a standard line in low-latency tuning recipes. What it switched off no longer exists.

## Before

```sh
JAVA_OPTS="-XX:-UseBiasedLocking -XX:+AlwaysPreTouch"
```

## After

```sh
JAVA_OPTS="-XX:+AlwaysPreTouch"
```

## Notes

- Nothing replaces it. Uncontended locking is fast without biasing on current JVMs.
