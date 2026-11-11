---
id: jvm-flags-redundant
title: 'Drop JVM options that are now defaults or dead weight'
category: runtime
kind: superseded
applies: java>=11
note: '-Djava.security.egd=file:/dev/./urandom and -XX:+UseContainerSupport do nothing on a current JVM, and -XX:MaxRAMFraction and -Xverify:none are deprecated. They only make startup lines harder to read.'
source: https://spring.io/guides/gs/spring-boot-docker
modern-since: 2018
detect:
  - 'code Dockerfile*,*.dockerfile,*.sh,*.yml,*.yaml,*.properties,*.conf,*.env,jvm.config,jvm.options,pom.xml,build.gradle,build.gradle.kts,Procfile,*.service,Makefile,*.bat,*.cmd /-Djava\.security\.egd=file:\/dev\/\.\/urandom|-XX:\+UseContainerSupport\b|-XX:(?:Max|Min|Initial)RAMFraction=|-Xverify:none|(?:^|\s|")-noverify\b/'
---

These are harmless today, which is why they survive in every copied Dockerfile.

## Before

```dockerfile
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-XX:+UseContainerSupport", "-XX:MaxRAMFraction=2", "-jar", "/app.jar"]
```

## After

```dockerfile
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50", "-jar", "/app.jar"]
```

## Notes

| Option | Status |
|---|---|
| `-Djava.security.egd=file:/dev/./urandom` | A Java 7 era workaround for slow Tomcat startup. Not needed since Java 8 |
| `-XX:+UseContainerSupport` | On by default since Java 10 |
| `-XX:MaxRAMFraction`, `-XX:MinRAMFraction`, `-XX:InitialRAMFraction` | Deprecated in Java 10 in favor of `-XX:MaxRAMPercentage`, `-XX:MinRAMPercentage`, `-XX:InitialRAMPercentage` |
| `-Xverify:none`, `-noverify` | Deprecated in Java 13. For faster startup use CDS or the AOT cache |
