---
id: jdbc-driver-loading
title: 'Drop Class.forName for JDBC drivers'
category: jdk-apis
kind: superseded
note: 'JDBC 4 (Java 6, 2006) loads drivers through the service loader. Class.forName("...Driver") is dead code, and com.mysql.jdbc.Driver is itself a deprecated alias.'
source: https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/DriverManager.html
modern-since: 2006
detect:
  - 'strings *.java /\bClass\.forName\(\s*"[\w.]*\.\w*[Dd]river"\s*\)/'
---

## Before

```java
Class.forName("com.mysql.jdbc.Driver");
Connection connection = DriverManager.getConnection(url, user, password);
```

## After

```java
Connection connection = dataSource.getConnection();
```

## Notes

- In an application, get connections from a pooled `DataSource` (HikariCP is Spring Boot's default), not from `DriverManager`.
- Spring Boot also infers `spring.datasource.driver-class-name` from the JDBC URL, so that property can usually go as well.
