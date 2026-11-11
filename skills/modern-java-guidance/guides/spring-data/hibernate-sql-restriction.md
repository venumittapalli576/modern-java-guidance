---
id: hibernate-sql-restriction
title: 'Use @SQLRestriction instead of @Where'
category: spring-data
kind: deprecated
applies: boot>=3.2
removed: boot>=4.0
note: 'Hibernate 6.3 deprecated @Where in favor of @SQLRestriction, and Hibernate 7 (Spring Boot 4) removed it.'
source: https://docs.hibernate.org/orm/7.0/migration-guide/
modern-since: 2023
detect:
  - 'code *.java /@Where(?:JoinTable)?\s*\(\s*clause\s*=/'
  - 'code *.java /^\s*import\s+org\.hibernate\.annotations\.Where(?:JoinTable)?\s*;/'
---

The classic soft-delete recipe pairs `@SQLDelete` with `@Where(clause = "deleted = false")`. Only the second annotation changed.

## Before

```java
import org.hibernate.annotations.Where;

@Entity
@Where(clause = "deleted = false")
public class Article {
}
```

## After

```java
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted = false")
public class Article {
}
```

## Notes

- `@WhereJoinTable` becomes `@SQLJoinTableRestriction`.
- For plain soft delete, Hibernate 6.4 and later have `@SoftDelete`, which handles both the delete statement and the filter.
