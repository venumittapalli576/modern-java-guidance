---
id: jvm-flags-cms
title: 'Remove CMS collector flags'
category: runtime
kind: removed
applies: java>=15
note: 'The CMS collector was removed in Java 14 (JEP 363). From Java 15, -XX:+UseConcMarkSweepGC and the other CMS options make the JVM exit at startup with "Unrecognized VM option".'
source: https://openjdk.org/jeps/363
modern-since: 2017
detect:
  - 'code Dockerfile*,*.dockerfile,*.sh,*.yml,*.yaml,*.properties,*.conf,*.env,jvm.config,jvm.options,pom.xml,build.gradle,build.gradle.kts,Procfile,*.service,Makefile,*.bat,*.cmd /-XX:[+-](?:UseConcMarkSweepGC|UseParNewGC|UseParallelOldGC|UseCMSInitiatingOccupancyOnly|CMSClassUnloadingEnabled|CMSParallelRemarkEnabled|CMSScavengeBeforeRemark|CMSIncrementalMode|CMSPermGenSweepingEnabled|ExplicitGCInvokesConcurrentAndUnloadsClasses)\b|-XX:CMSInitiatingOccupancyFraction=/'
---

"Low-pause" settings from the Java 6 to 8 era all name CMS. G1 has been the default collector since Java 9, and there is nothing to carry over. This was verified on JDK 21.

## Before

```sh
exec java -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:CMSInitiatingOccupancyFraction=70 \
    -XX:+UseCMSInitiatingOccupancyOnly -XX:+CMSClassUnloadingEnabled -jar app.jar
```

## After

```sh
exec java -XX:MaxRAMPercentage=75 -jar app.jar
```

## Notes

- Start with no collector flags. G1 is the default, and Java 27 makes it the default on small machines too (JEP 523).
- If pause time is the goal, set a target with `-XX:MaxGCPauseMillis=<ms>`, or use ZGC (`-XX:+UseZGC`), which is generational since Java 21 and generational-only since Java 24.
- `-XX:+UseParallelOldGC` went the same way. `-XX:+UseParallelGC` covers it.
