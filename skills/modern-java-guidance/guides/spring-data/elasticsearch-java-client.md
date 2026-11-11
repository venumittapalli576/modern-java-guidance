---
id: elasticsearch-java-client
title: 'Use the Elasticsearch Java API Client instead of RestHighLevelClient'
category: spring-data
kind: deprecated
removed: boot>=3.0
note: 'Elastic deprecated RestHighLevelClient in 7.15, and Spring Boot 3 removed its auto-configuration. The replacement is ElasticsearchClient from co.elastic.clients:elasticsearch-java.'
source: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide
modern-since: 2021
limit: 1
detect:
  - 'code *.java /\bRestHighLevelClient\b/'
---

## Before

```java
RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200)));

SearchRequest request = new SearchRequest("products");
request.source(new SearchSourceBuilder().query(QueryBuilders.matchQuery("name", term)));
SearchResponse response = client.search(request, RequestOptions.DEFAULT);
```

## After

```java
SearchResponse<Product> response = elasticsearchClient.search(s -> s
        .index("products")
        .query(q -> q.match(m -> m.field("name").query(term))),
        Product.class);
```

## Notes

- Spring Boot auto-configures an `ElasticsearchClient` bean from `spring.elasticsearch.uris`.
- With Spring Data Elasticsearch, repositories and `ElasticsearchOperations` hide the client. `ElasticsearchRestTemplate` became `ElasticsearchTemplate`.
