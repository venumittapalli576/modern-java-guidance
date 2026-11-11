---
id: mockito-extension
title: 'Use MockitoExtension instead of MockitoAnnotations.initMocks'
category: spring-testing
kind: deprecated
note: 'MockitoAnnotations.initMocks() has been deprecated since Mockito 3.4 because it never releases its mocks. With JUnit Jupiter, MockitoExtension initializes and cleans up @Mock fields.'
source: https://github.com/mockito/mockito/releases/tag/v3.4.0
modern-since: 2018
detect:
  - 'code *.java /MockitoAnnotations\.initMocks\s*\(/'
---

The extension also turns on strict stubbing, which fails a test that sets up stubs it never uses.

## Before

```java
class InvoiceServiceTest {

    @Mock
    InvoiceRepository invoices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }
}
```

## After

```java
@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    InvoiceRepository invoices;

    @InjectMocks
    InvoiceService service;
}
```

## Notes

- If you cannot use the extension, `MockitoAnnotations.openMocks(this)` returns an `AutoCloseable` that must be closed after each test.
