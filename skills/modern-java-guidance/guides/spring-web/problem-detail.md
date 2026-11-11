---
id: problem-detail
title: 'Return ProblemDetail for API errors instead of ad hoc error bodies'
category: spring-web
kind: superseded
applies: boot>=3.0
advice-only: true
note: 'Spring Framework 6 supports RFC 9457 problem details natively through ProblemDetail and ErrorResponseException, producing application/problem+json.'
source: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
modern-since: 2022
---

Hand-written error DTOs and `Map<String, Object>` bodies give every API a different error shape. Spring MVC and WebFlux can render the standard problem details format for both your exceptions and the framework's own.

## After

```java
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    ProblemDetail handle(OrderNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Order not found");
        problem.setProperty("orderId", ex.orderId());
        return problem;
    }
}
```

## Notes

- Extending `ResponseEntityExceptionHandler` makes Spring's built-in exceptions (validation failures, unsupported media type and so on) render as problem details too. Setting `spring.mvc.problemdetails.enabled=true` does the same without a class.
- For a one-off, throw `new ErrorResponseException(HttpStatus.CONFLICT, problem, null)`.
