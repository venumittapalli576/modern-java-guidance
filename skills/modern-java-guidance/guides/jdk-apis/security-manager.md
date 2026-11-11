---
id: security-manager
title: 'Do not use the Security Manager'
category: jdk-apis
kind: deprecated
applies: java>=17
note: 'The Security Manager was deprecated for removal in Java 17 (JEP 411) and permanently disabled in Java 24 (JEP 486), where System.setSecurityManager always throws.'
source: https://openjdk.org/jeps/486
modern-since: 2021
detect:
  - 'code *.java /\bSystem\.(?:get|set)SecurityManager\s*\(/'
  - 'code *.java /\bAccessController\.doPrivileged\s*\(/'
---

## Before

```java
SecurityManager sm = System.getSecurityManager();
if (sm != null) {
    sm.checkPermission(new RuntimePermission("shutdownHooks"));
}
String home = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("user.home"));
```

## After

```java
String home = System.getProperty("user.home");
```

## Notes

- Sandboxing belongs outside the JVM now: containers, seccomp profiles, operating system users and network policy.
- To stop `System.exit` in tests or plugins, restructure the code so that the exit is a returned status, not something to intercept.
