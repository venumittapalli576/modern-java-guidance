---
id: spring-batch-builders
title: 'Build jobs and steps with JobBuilder and StepBuilder, not the builder factories'
category: spring-batch
kind: deprecated
applies: boot>=3.0
removed: boot>=4.0
note: 'JobBuilderFactory and StepBuilderFactory have been deprecated for removal since Spring Batch 5.0 (Spring Boot 3). Builders now take the JobRepository, and a chunk step names its transaction manager.'
source: https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-5.0-Migration-Guide
modern-since: 2022
limit: 1
detect:
  - 'code *.java /\b(?:JobBuilderFactory|StepBuilderFactory)\b/'
---

Every Spring Batch tutorial older than 2023 starts by autowiring `JobBuilderFactory` and adding `@EnableBatchProcessing`. On Spring Boot 3 the first is deprecated and the second switches off Boot's Batch auto-configuration.

## Before

```java
@Configuration
@EnableBatchProcessing
public class ImportJobConfig {

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory steps;

    @Bean
    public Job importCustomers(Step loadStep) {
        return jobs.get("importCustomers").start(loadStep).build();
    }
}
```

## After

```java
@Configuration
public class ImportJobConfig {

    @Bean
    Job importCustomers(JobRepository jobRepository, Step loadStep) {
        return new JobBuilder("importCustomers", jobRepository)
                .start(loadStep)
                .build();
    }

    @Bean
    Step loadStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                  ItemReader<CustomerRow> reader, ItemWriter<Customer> writer) {
        return new StepBuilder("loadStep", jobRepository)
                .<CustomerRow, Customer>chunk(100, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }
}
```

## Notes

- Leave out `@EnableBatchProcessing` in a Spring Boot application unless you intend to take over the Batch infrastructure yourself.
- The After example is the Spring Batch 5 form. Spring Batch 6 (Spring Boot 4) changes the chunk step builder again and stores job metadata in memory unless you add `spring-boot-starter-batch-jdbc`. Check its migration guide when you move.
