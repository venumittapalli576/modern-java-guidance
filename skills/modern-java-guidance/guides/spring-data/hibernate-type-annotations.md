---
id: hibernate-type-annotations
title: 'Use @JdbcTypeCode instead of string-based @Type and @TypeDef'
category: spring-data
kind: removed
applies: boot>=3.0
note: 'Hibernate 6 removed @TypeDef and the string form @Type(type = "..."). JSON, UUID and array columns map with @JdbcTypeCode(SqlTypes.JSON) and friends, with no extra library.'
source: https://docs.jboss.org/hibernate/orm/6.0/migration-guide/migration-guide.html
modern-since: 2022
detect:
  - 'code *.java /@Type\s*\(\s*type\s*=/'
  - 'code *.java /@TypeDefs?\b/'
---

Mapping a `jsonb` column used to mean a third-party type library and a `@TypeDef`. Hibernate 6 has JSON, UUID, array and enum mappings built in.

## Before

```java
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Product {

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
}
```

## After

```java
@Entity
public class Product {

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> attributes;
}
```

## Notes

- `@JdbcTypeCode(SqlTypes.ARRAY)` maps arrays and collections of basic types. `java.util.UUID` and `java.time` types need no annotation.
- A genuinely custom type implements `UserType` and is referenced by class: `@Type(MoneyType.class)`.
