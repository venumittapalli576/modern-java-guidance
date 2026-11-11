---
id: jjwt-current-api
title: 'Use the JJWT 0.12 builder and parser API'
category: libraries
kind: deprecated
note: 'JJWT 0.12 (2023) deprecated setSigningKey, parseClaimsJws, setSubject and signWith(SignatureAlgorithm, ...). The single io.jsonwebtoken:jjwt 0.9.x jar also needs javax.xml.bind, which Java 11 removed.'
source: https://github.com/jwtk/jjwt/blob/master/CHANGELOG.md
modern-since: 2023
detect:
  - 'code *.java /\.setSigningKey\s*\(|\.parseClaimsJws\s*\(|\bJwts\.parserBuilder\s*\(|\bSignatureAlgorithm\.[HREP]S\d{3}\b/'
  - 'raw pom.xml,build.gradle,build.gradle.kts /<artifactId>jjwt<\/artifactId>|io\.jsonwebtoken:jjwt:/'
---

The JWT utility class in most Spring Security tutorials is written against JJWT 0.9.1 from 2018. On Java 11 or later it fails at run time with `NoClassDefFoundError: javax/xml/bind/DatatypeConverter`, and none of its method names survive in the current API.

## Before

```java
String token = Jwts.builder()
        .setSubject(username)
        .setExpiration(expiry)
        .signWith(SignatureAlgorithm.HS256, secret)
        .compact();

Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
```

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>
```

## After

```java
SecretKey key = Keys.hmacShaKeyFor(secretBytes);

String token = Jwts.builder()
        .subject(username)
        .expiration(Date.from(now.plus(Duration.ofMinutes(15))))
        .signWith(key)
        .compact();

Claims claims = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
```

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>${jjwt.version}</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>${jjwt.version}</version>
    <scope>runtime</scope>
</dependency>
```

## Notes

- The signing key is a `SecretKey` of adequate length, not a raw string. The algorithm is inferred from the key.
- If the application only validates tokens issued by an identity provider, you do not need JJWT or a hand-written filter. Use Spring Security's resource server support: `http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))`.
