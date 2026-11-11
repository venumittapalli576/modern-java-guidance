---
id: jvm-flags-removed
title: 'Remove JVM options that no longer exist'
category: runtime
kind: removed
applies: java>=17
note: 'PermGen was replaced by Metaspace in Java 8. Since Java 17, -XX:MaxPermSize and -XX:PermSize stop the JVM from starting, as do -XX:+AggressiveOpts, -XX:+UseCGroupMemoryLimitForHeap and -d64.'
source: https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html
modern-since: 2014
detect:
  - 'code Dockerfile*,*.dockerfile,*.sh,*.yml,*.yaml,*.properties,*.conf,*.env,jvm.config,jvm.options,pom.xml,build.gradle,build.gradle.kts,Procfile,*.service,Makefile,*.bat,*.cmd /-XX:(?:Max)?PermSize=|-XX:[+-](?:AggressiveOpts|UseCGroupMemoryLimitForHeap|UnlockCommercialFeatures|UseAppCDS|UseSplitVerifier|UseFastAccessorMethods)\b|(?:^|\s|")-d64\b|-Djava\.(?:endorsed|ext)\.dirs=|-Xbootclasspath\/p:/'
---

HotSpot tolerates an obsolete option for a release or two and then rejects it. Each option here ends startup with `Unrecognized VM option` and `Could not create the Java Virtual Machine`. This was verified on JDK 21.

## Before

```yaml
services:
  app:
    environment:
      JAVA_TOOL_OPTIONS: "-Xms512m -Xmx512m -XX:MaxPermSize=256m -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap"
```

## After

```yaml
services:
  app:
    environment:
      JAVA_TOOL_OPTIONS: "-XX:MaxRAMPercentage=75"
```

## Notes

| Removed option | What to do |
|---|---|
| `-XX:PermSize`, `-XX:MaxPermSize` | Delete. To cap class metadata, `-XX:MaxMetaspaceSize` |
| `-XX:+UseCGroupMemoryLimitForHeap` | Delete. Container limits are honored by default since Java 10. Size the heap with `-XX:MaxRAMPercentage` |
| `-XX:+AggressiveOpts`, `-XX:+UseFastAccessorMethods`, `-XX:+UseSplitVerifier` | Delete |
| `-XX:+UnlockCommercialFeatures` | Delete. Flight Recorder is free and open since Java 11 |
| `-XX:+UseAppCDS` | Delete. Use `-XX:SharedArchiveFile`, or the AOT cache on Java 24 and later |
| `-d64` | Delete. Every supported JVM is 64-bit |
| `-Djava.endorsed.dirs`, `-Djava.ext.dirs`, `-Xbootclasspath/p:` | Put the jars on the class path or module path |

- In a container, prefer one percentage (`-XX:MaxRAMPercentage`) over fixed `-Xms` and `-Xmx`, so the heap follows the memory limit.
