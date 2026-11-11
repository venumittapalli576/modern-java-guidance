---
id: aws-sdk-v2
title: 'Use AWS SDK for Java 2.x instead of 1.x'
category: libraries
kind: deprecated
note: 'AWS SDK for Java 1.x (com.amazonaws) reached end of support on December 31, 2025. The 2.x SDK (software.amazon.awssdk) is the supported line.'
source: https://aws.amazon.com/blogs/developer/announcing-end-of-support-for-aws-sdk-for-java-v1-x-on-december-31-2025/
modern-since: 2018
limit: 1
detect:
  - 'code *.java /^\s*import\s+com\.amazonaws\.(?!services\.lambda\.runtime\b|xray\b)/'
  - 'raw pom.xml,build.gradle,build.gradle.kts /\baws-java-sdk\b/'
---

`AmazonS3ClientBuilder`, `AmazonDynamoDBClientBuilder` and `BasicAWSCredentials` are the 1.x API, which no longer receives fixes.

## Before

```java
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();
s3.putObject("reports", "2026/q3.pdf", file);
```

```xml
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
</dependency>
```

## After

```java
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

S3Client s3 = S3Client.builder().region(Region.US_EAST_1).build();
s3.putObject(
        PutObjectRequest.builder().bucket("reports").key("2026/q3.pdf").build(),
        RequestBody.fromFile(path));
```

```xml
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
</dependency>
```

## Notes

- 2.x clients are immutable and closeable. Requests are built with builders, and every service has an async client.
- `com.amazonaws.services.lambda.runtime` is the Lambda runtime interface, not SDK 1.x, and is still current. This guide leaves it alone.
- Import the BOM `software.amazon.awssdk:bom` to keep module versions aligned.
