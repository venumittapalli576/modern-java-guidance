---
id: jvm-flags-gc-logging
title: 'Use -Xlog:gc instead of the Java 8 GC logging flags'
category: runtime
kind: removed
applies: java>=9
note: 'Unified logging (Java 9, JEP 271) replaced the GC logging flags. With -XX:+PrintGCDateStamps or -XX:+UseGCLogFileRotation the JVM exits at startup with "Unrecognized VM option".'
source: https://openjdk.org/jeps/271
modern-since: 2017
detect:
  - 'code Dockerfile*,*.dockerfile,*.sh,*.yml,*.yaml,*.properties,*.conf,*.env,jvm.config,jvm.options,pom.xml,build.gradle,build.gradle.kts,Procfile,*.service,Makefile,*.bat,*.cmd /-XX:[+-](?:PrintGCDateStamps|PrintGCTimeStamps|UseGCLogFileRotation|PrintTenuringDistribution|PrintGCApplicationStoppedTime|PrintGCApplicationConcurrentTime|PrintHeapAtGC|PrintAdaptiveSizePolicy|PrintReferenceGC|PrintPromotionFailure)\b|-XX:(?:NumberOfGCLogFiles|GCLogFileSize)=/'
---

GC logging options from Java 8 tuning guides are copied into Dockerfiles and start scripts more than any other flags. Every one in this list stops a current JVM from starting. This was verified on JDK 21.

## Before

```dockerfile
ENV JAVA_OPTS="-Xloggc:/var/log/app/gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
    -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M"
```

## After

```dockerfile
ENV JAVA_OPTS="-Xlog:gc*:file=/var/log/app/gc.log:time,uptime:filecount=5,filesize=10M"
```

## Notes

- `-XX:+PrintGCDetails`, `-XX:+PrintGC` and `-Xloggc:` are still accepted, with a deprecation warning, and are mapped to `-Xlog`. Replace them in the same edit.
- `-Xlog:gc` logs one line per collection. `-Xlog:gc*` adds the detail that `PrintGCDetails` used to.
- `-Xlog:safepoint` replaces `PrintGCApplicationStoppedTime`, and `-Xlog:gc+age=trace` replaces `PrintTenuringDistribution`.
