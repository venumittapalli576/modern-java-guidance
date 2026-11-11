---
id: hibernate-criteria-api
title: 'Use JPA Criteria or HQL instead of the legacy Hibernate Criteria API'
category: spring-data
kind: deprecated
removed: boot>=3.0
note: 'Session.createCriteria() and org.hibernate.criterion were deprecated in Hibernate 5.2 and removed in 6.0 (Spring Boot 3).'
source: https://docs.jboss.org/hibernate/orm/6.0/migration-guide/migration-guide.html
modern-since: 2009
when-file: '\borg\.hibernate\b|\bSessionFactory\b|\bgetCurrentSession\b|\bEntityManager\b'
detect:
  - 'code *.java /^\s*import\s+org\.hibernate\.(?:criterion\.|Criteria\s*;)/'
  - 'code *.java /\.createCriteria\s*\(/'
---

`Restrictions.eq(...)` and `session.createCriteria(...)` are everywhere in older Hibernate material. The classes do not exist in Hibernate 6.

## Before

```java
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

List<Order> open = session.createCriteria(Order.class)
        .add(Restrictions.eq("status", OrderStatus.OPEN))
        .list();
```

## After

```java
List<Order> open = entityManager
        .createQuery("select o from Order o where o.status = :status", Order.class)
        .setParameter("status", OrderStatus.OPEN)
        .getResultList();
```

## Notes

- In a Spring Data application the usual answer is a derived query method, `List<Order> findByStatus(OrderStatus status)`, or a `Specification` for dynamic filters.
- For type-safe dynamic queries, use the JPA `CriteriaBuilder`.
