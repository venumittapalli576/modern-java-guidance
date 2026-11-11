---
id: legacy-collections
title: 'Use ArrayList, HashMap, ArrayDeque and StringBuilder instead of Vector, Hashtable, Stack and StringBuffer'
category: language
kind: superseded
note: 'Vector, Hashtable, Stack and StringBuffer lock on every call. The Collections Framework (Java 1.2, 1998), StringBuilder (Java 5) and ArrayDeque (Java 6) replaced them.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Vector.html
modern-since: 1998
detect:
  - 'code *.java /\bnew\s+(?:Vector|Hashtable|Stack|StringBuffer)\s*[<(]/'
---

## Before

```java
Vector<String> names = new Vector<>();
Hashtable<String, Integer> counts = new Hashtable<>();
Stack<Frame> frames = new Stack<>();
StringBuffer sb = new StringBuffer();
```

## After

```java
List<String> names = new ArrayList<>();
Map<String, Integer> counts = new HashMap<>();
Deque<Frame> frames = new ArrayDeque<>();
StringBuilder sb = new StringBuilder();
```

## Notes

- Per-method locking does not make compound operations such as check-then-put safe. When threads really share the structure, use `ConcurrentHashMap`, `CopyOnWriteArrayList` or `ConcurrentLinkedDeque`.
- With `Deque`, `push`, `pop` and `peek` work as they did on `Stack`.
