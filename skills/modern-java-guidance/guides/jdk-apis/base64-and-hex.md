---
id: base64-and-hex
title: 'Use java.util.Base64 and HexFormat instead of DatatypeConverter or sun.misc classes'
category: jdk-apis
kind: removed
applies: java>=11
note: 'javax.xml.bind left the JDK in Java 11 (JEP 320) and sun.misc.BASE64Encoder in Java 9, so DatatypeConverter calls fail with NoClassDefFoundError. Use Base64 (Java 8) and HexFormat (Java 17).'
source: https://openjdk.org/jeps/320
modern-since: 2014
detect:
  - 'code *.java /\bDatatypeConverter\b/'
  - 'code *.java /\bBASE64(?:En|De)coder\b/'
---

`DatatypeConverter.printHexBinary` and `parseBase64Binary` were the handy one-liners of Java 8, which is why they are still everywhere in hashing and token examples.

## Before

```java
String token = DatatypeConverter.printBase64Binary(bytes);
String digest = DatatypeConverter.printHexBinary(sha256.digest(payload));
byte[] decoded = new BASE64Decoder().decodeBuffer(token);
```

## After

```java
String token = Base64.getEncoder().encodeToString(bytes);
String digest = HexFormat.of().formatHex(sha256.digest(payload));
byte[] decoded = Base64.getDecoder().decode(token);
```

## Notes

- `Base64.getUrlEncoder().withoutPadding()` gives the URL-safe variant used in JWTs.
- `HexFormat` needs Java 17. On Java 11 to 16, format each byte with `String.format("%02x", b)`.
- JAXB itself is still available as a dependency (`jakarta.xml.bind`), but pulling it in for hex and Base64 is not worth it.
