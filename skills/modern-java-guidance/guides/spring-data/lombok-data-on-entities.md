---
id: lombok-data-on-entities
title: 'Do not put Lombok @Data on JPA entities'
category: spring-data
kind: hazard
note: '@Data generates equals, hashCode and toString over every field. On an entity that triggers lazy loading, changes the hash when the id is assigned, and can recurse through bidirectional relations.'
source: https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#mapping-model-pojo-equalshashcode
modern-since: 2014
when-file: '@Entity\b'
detect:
  - 'code *.java /^\s*@Data\b/'
---

`@Data` is right for a plain value holder and wrong for an entity. An entity's identity is its primary key, not the current value of all its columns and collections.

## Before

```java
@Data
@Entity
public class PurchaseOrder {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "order")
    private List<OrderLine> lines = new ArrayList<>();
}
```

## After

```java
@Getter
@Setter
@Entity
public class PurchaseOrder {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "order")
    private List<OrderLine> lines = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        return o instanceof PurchaseOrder other && id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return PurchaseOrder.class.hashCode();
    }
}
```

## Notes

- The constant `hashCode` is deliberate: it keeps an entity in the same hash bucket before and after its id is generated.
- For read-only projections and DTOs, use a record.
