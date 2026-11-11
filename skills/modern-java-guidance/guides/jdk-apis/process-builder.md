---
id: process-builder
title: 'Use ProcessBuilder instead of Runtime.exec(String)'
category: jdk-apis
kind: superseded
note: 'Runtime.exec(String) splits the command on whitespace with no quoting rules, and its single-string overloads have been deprecated since Java 18. ProcessBuilder takes arguments as a list.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Runtime.html
modern-since: 2004
detect:
  - 'code *.java /\bRuntime\.getRuntime\(\)\s*\.\s*exec\s*\(/'
---

## Before

```java
Process process = Runtime.getRuntime().exec("git log --oneline -n " + count);
```

## After

```java
Process process = new ProcessBuilder("git", "log", "--oneline", "-n", String.valueOf(count))
        .redirectErrorStream(true)
        .start();
```

## Notes

- Passing each argument separately means a value containing spaces or shell characters cannot change the command.
- Always consume the output stream, or redirect it, and call `waitFor` with a timeout. A full pipe buffer blocks the child process.
