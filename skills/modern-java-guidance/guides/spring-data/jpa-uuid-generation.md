---
id: jpa-uuid-generation
title: 'Generate UUID keys with GenerationType.UUID instead of @GenericGenerator'
category: spring-data
kind: superseded
applies: boot>=3.0
deprecated: boot>=3.3
note: 'Jakarta Persistence 3.1 has GenerationType.UUID, and Hibernate deprecated @GenericGenerator in 6.5 (Spring Boot 3.3). The "uuid2" strategy string is no longer needed.'
source: https://docs.hibernate.org/orm/6.5/migration-guide/migration-guide.html
modern-since: 2022
detect:
  - 'code *.java /@GenericGenerator\b/'
---

`@GenericGenerator` with a strategy name is a Hibernate 4 idiom. The standard annotation now covers UUIDs.

## Before

```java
@Id
@GeneratedValue(generator = "uuid")
@GenericGenerator(name = "uuid", strategy = "uuid2")
private UUID id;
```

## After

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

## Notes

- For time-ordered UUIDs, which index better, Hibernate has `@UuidGenerator(style = UuidGenerator.Style.TIME)`.
- A custom generator is declared with a meta-annotation marked `@IdGeneratorType`, not with a strategy string.
