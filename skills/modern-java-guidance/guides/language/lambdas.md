---
id: lambdas
title: 'Use lambdas and method references instead of anonymous classes'
category: language
kind: superseded
note: 'Lambdas arrived in Java 8 (2014). An anonymous Runnable, Comparator or Callable is four lines of ceremony around one expression.'
source: https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
modern-since: 2014
detect:
  - 'code *.java /\bnew\s+(?:Runnable|Callable<[^>]*>|Comparator<[^>]*>|Supplier<[^>]*>|Function<[^>]*>|Consumer<[^>]*>|Predicate<[^>]*>|ActionListener)\s*\(\s*\)\s*\{/'
---

## Before

```java
customers.sort(new Comparator<Customer>() {
    @Override
    public int compare(Customer a, Customer b) {
        return a.getLastName().compareTo(b.getLastName());
    }
});

executor.submit(new Runnable() {
    @Override
    public void run() {
        reportService.generate();
    }
});
```

## After

```java
customers.sort(Comparator.comparing(Customer::getLastName));

executor.submit(reportService::generate);
```

## Notes

- An anonymous class is still the right tool when the implementation needs its own fields or more than one method.
