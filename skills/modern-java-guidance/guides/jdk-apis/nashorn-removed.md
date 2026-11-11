---
id: nashorn-removed
title: 'Do not rely on the built-in JavaScript engine'
category: jdk-apis
kind: removed
applies: java>=15
note: 'Nashorn was removed in Java 15 (JEP 372). getEngineByName("nashorn") or "JavaScript" now returns null, so the next call throws NullPointerException.'
source: https://openjdk.org/jeps/372
modern-since: 2020
detect:
  - 'strings *.java /getEngineByName\(\s*"(?:nashorn|[jJ]ava[sS]cript|js|ecmascript)"/'
---

Evaluating a formula with `ScriptEngineManager` is a familiar trick from Java 8. No script engine ships with the JDK any more.

## Before

```java
ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
Object result = engine.eval("6 * 7");
```

## After

```java
try (Context context = Context.create("js")) {
    int result = context.eval("js", "6 * 7").asInt();
}
```

## Notes

- The After example uses GraalJS through the GraalVM polyglot API (`org.graalvm.polyglot:polyglot` and `org.graalvm.polyglot:js`), which runs on any JDK.
- If all you need is arithmetic or a rule expression, a small expression library or Spring's `SpelExpressionParser` is lighter and safer than a JavaScript engine.
