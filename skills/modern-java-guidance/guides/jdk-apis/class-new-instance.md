---
id: class-new-instance
title: 'Use getDeclaredConstructor().newInstance() instead of Class.newInstance()'
category: jdk-apis
kind: deprecated
applies: java>=9
note: 'Class.newInstance() has been deprecated since Java 9 because it rethrows any checked exception from the constructor without declaring it.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Class.html
modern-since: 2017
detect:
  - 'code *.java /\.class\s*\.\s*newInstance\(\)|\bClass\.forName\([^()]*\)\s*\.\s*newInstance\(\)/'
---

## Before

```java
Plugin plugin = (Plugin) Class.forName(className).newInstance();
```

## After

```java
Plugin plugin = (Plugin) Class.forName(className).getDeclaredConstructor().newInstance();
```

## Notes

- For plugin discovery, `ServiceLoader.load(Plugin.class)` avoids reflection on class names altogether.
