---
id: jdk-http-client
title: 'Use java.net.http.HttpClient instead of HttpURLConnection or Apache HttpClient 4'
category: jdk-apis
kind: superseded
applies: java>=11
note: 'The JDK has had a modern HTTP client since Java 11 with HTTP/2, timeouts and async calls. HttpURLConnection and the org.apache.http (HttpClient 4.x) line are the legacy options.'
source: https://openjdk.org/jeps/321
modern-since: 2018
limit: 1
detect:
  - 'code *.java /\(\s*Https?URLConnection\s*\)/'
  - 'code *.java /^\s*import\s+org\.apache\.http\.(?:client\.methods\.|client\.HttpClient\b|impl\.client\.(?:HttpClients|CloseableHttpClient|DefaultHttpClient|HttpClientBuilder)\b)/'
---

## Before

```java
HttpURLConnection connection = (HttpURLConnection) url.openConnection();
connection.setRequestMethod("GET");
connection.setConnectTimeout(5000);
try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
    return reader.lines().collect(Collectors.joining());
}
```

```java
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
```

## After

```java
HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.example.com/rates"))
        .timeout(Duration.ofSeconds(10))
        .header("Accept", "application/json")
        .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
```

## Notes

- Create one `HttpClient` and reuse it. It owns the connection pool.
- In a Spring application, use `RestClient`, which can run on top of the JDK client and adds message conversion and error handling.
- If you need Apache HttpClient's features, use the 5.x line (`org.apache.hc.client5`). Spring Framework 6 and later only support that one.
- Java 26 added HTTP/3 to the JDK client.
