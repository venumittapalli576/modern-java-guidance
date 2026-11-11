---
id: hibernate-dialect-autodetect
title: 'Remove version-specific Hibernate dialects and let Hibernate detect the database'
category: spring-data
kind: deprecated
applies: boot>=3.0
removed: boot>=4.0
note: 'Hibernate 6 works out the dialect and database version from the JDBC connection. Classes such as MySQL8Dialect and PostgreSQL95Dialect are deprecated in 6 and removed in 7 (Spring Boot 4).'
source: https://docs.hibernate.org/orm/7.0/migration-guide/
modern-since: 2022
detect:
  - 'strings application*.properties,application*.yml,application*.yaml,persistence.xml,hibernate.cfg.xml,*.java /org\.hibernate\.dialect\.(?:MySQL5\w*|MySQL55|MySQL57|MySQL8|MariaDB\d+|PostgreSQL\d+|PostgresPlus|Oracle\d+\w*|SQLServer20\d\d|DB2\d+\w*)Dialect\b/'
---

A `hibernate.dialect` line is in nearly every Spring Data JPA tutorial. Since Hibernate 6 it is unnecessary, and naming a version-specific class that no longer exists stops the application from starting.

## Before

```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate
```

```yaml
spring:
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQL95Dialect
```

## After

```properties
spring.jpa.hibernate.ddl-auto=validate
```

## Notes

- If you must set a dialect, for example when no connection is available at startup, use the unversioned class: `MySQLDialect`, `PostgreSQLDialect`, `OracleDialect`, `SQLServerDialect`, `MariaDBDialect`.
- `MySQL5InnoDBDialect` and the other storage-engine variants are gone too. InnoDB is the default.
