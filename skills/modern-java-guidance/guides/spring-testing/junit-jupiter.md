---
id: junit-jupiter
title: 'Write tests with JUnit Jupiter, not JUnit 4'
category: spring-testing
kind: superseded
deprecated: boot>=4.0
note: 'JUnit Jupiter has been Spring Boot''s default since 2.2, and the vintage engine left spring-boot-starter-test in 2.4. Spring Framework 7 deprecates its JUnit 4 support, including SpringRunner.'
source: https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes
modern-since: 2017
limit: 1
detect:
  - 'code *.java /^\s*import\s+(?:static\s+)?org\.junit\.(?:Test|Before|After|BeforeClass|AfterClass|Ignore|Assert|Assume|Rule|ClassRule|runner|runners|rules|experimental)\b/'
  - 'raw pom.xml,build.gradle,build.gradle.kts /<artifactId>junit<\/artifactId>|[''"]junit:junit:|junit-vintage-engine/'
---

A JUnit 4 test in a current Spring Boot project is either not discovered at all, so it silently never runs, or it forces the vintage engine back onto the classpath.

## Before

```java
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PriceCalculatorTest {

    @Before
    public void setUp() {
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeQuantity() {
        calculator.priceFor(-1);
    }
}
```

```xml
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <scope>test</scope>
</dependency>
```

## After

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PriceCalculatorTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThatThrownBy(() -> calculator.priceFor(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

## Notes

| JUnit 4 | JUnit Jupiter |
|---|---|
| `@Before`, `@After` | `@BeforeEach`, `@AfterEach` |
| `@BeforeClass`, `@AfterClass` | `@BeforeAll`, `@AfterAll` |
| `@Ignore` | `@Disabled` |
| `@RunWith(SpringRunner.class)` | Nothing. `@SpringBootTest` and the slice annotations already register the Spring extension |
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
| `@Rule TemporaryFolder` | `@TempDir Path` |
| `@Test(expected = X.class)` | `assertThrows(X.class, ...)` or AssertJ `assertThatThrownBy` |

- Test classes and methods no longer need to be `public`.
