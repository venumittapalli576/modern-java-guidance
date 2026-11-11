---
id: files-read-string
title: 'Use Files.readString and Files.writeString for whole-file text I/O'
category: language
kind: superseded
applies: java>=11
note: 'Files.readString and writeString (Java 11) replace new String(Files.readAllBytes(...)) and Files.write(path, text.getBytes()), and always use UTF-8 unless told otherwise.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/nio/file/Files.html
modern-since: 2018
detect:
  - 'code *.java /\bnew\s+String\s*\(\s*Files\.readAllBytes\s*\(/'
---

## Before

```java
String template = new String(Files.readAllBytes(Paths.get("template.html")));
Files.write(Paths.get("report.txt"), report.getBytes());
```

## After

```java
String template = Files.readString(Path.of("template.html"));

Files.writeString(Path.of("report.txt"), report);
```

## Notes

- Before Java 18, `FileReader` and `FileWriter` used the platform default charset, a classic source of files that break on another machine.
- For large files, stream with `Files.lines(path)` or `Files.newBufferedReader(path)` instead of loading everything.
- `Path.of` (Java 11) replaces `Paths.get`.
