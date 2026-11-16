# Contributing

The most useful contribution is a guide: one legacy pattern that agents (or people) still write, with its current replacement. The second most useful is a correction. If a version number, a status or an example here is wrong, please open an issue or a pull request with a link to the source.

## Add a guide

A guide is a single Markdown file at `skills/modern-java-guidance/guides/<category>/<id>.md`. It is the documentation an agent reads and the rule the scanner enforces, in one place.

````markdown
---
id: rest-client
title: 'Use RestClient instead of RestTemplate'
category: spring-web
kind: superseded
applies: boot>=3.2
deprecated: boot>=4.2
note: 'RestClient (Spring Framework 6.1) is the replacement for blocking HTTP calls. RestTemplate is deprecated for removal as of Spring Framework 7.1 (Spring Boot 4.2) and is removed in 8.0.'
source: https://spring.io/blog/2025/09/30/the-state-of-http-clients-in-spring/
modern-since: 2023
limit: 1
detect:
  - 'code *.java /\bRestTemplate(?:Builder)?\b/'
---

One or two sentences on why this matters.

## Before

```java
RestTemplate restTemplate = new RestTemplate();
```

## After

```java
RestClient restClient = builder.baseUrl("https://inventory.example.com").build();
```

## Notes

- Anything a careful reviewer would add: exceptions, migration tips, related changes.
````

### Front matter

The front matter is valid YAML, so GitHub shows it as a table. Put `title`, `note`, `when-file` and every `detect` entry in single quotes. Inside single quotes nothing is escaped, which keeps regular expressions readable. Write a literal single quote as two (`''`).

| Key | Required | Meaning |
|---|---|---|
| `id` | yes | Must equal the file name without `.md` |
| `title` | yes | An instruction: "Use X instead of Y" |
| `category` | yes | Must equal the directory name |
| `kind` | yes | The status when the guide applies and nothing escalates it: `superseded`, `hazard`, `deprecated` or `removed` |
| `applies` | no | When the guide is relevant at all, usually the version where the replacement became available. Below it the guide is silent. Conditions use `java` or `boot` with `>=` or `<`, joined by commas: `boot>=3.2, java>=17` |
| `deprecated` | no | From here the status is at least `deprecated` |
| `removed` | no | From here the status is `removed` |
| `note` | yes | One or two sentences, at most 220 characters, shown next to a finding. Say what changed and in which version |
| `source` | yes | An `https` link to a primary source: a JEP, official documentation, a migration guide or release notes |
| `modern-since` | yes | The year the replacement became generally available. Where the replacement is very old and only the deprecation is recent, use the year of the deprecation |
| `detect` | yes | A list of one or more detectors, see below. Use `advice-only: true` instead for a guide that cannot be detected |
| `when-file` | no | A regular expression that must match the file (comments and string contents removed) for any finding to be reported. Use it to require context, such as an import |
| `limit` | no | The most findings to report per file |

`boot` conditions are Spring Boot versions. The scanner maps everything in the Spring portfolio through the Boot version, because that is the number people know. A guide with a `boot` condition is silent in a project that does not use Spring Boot.

### Detectors

```yaml
detect:
  - '<scope> <globs> /<regex>/<flags>'
```

- **scope** is what the regular expression sees:
  - `code`: comments removed and string contents blanked. The default choice for Java.
  - `strings`: comments removed, strings kept. For patterns inside string literals, and for Gradle files where coordinates are strings.
  - `comments`: only the comments.
  - `raw`: the file as it is.
- **globs** are comma-separated file name patterns such as `*.java` or `pom.xml,build.gradle,build.gradle.kts`. A glob with a `/` is matched against the path.
- **regex** uses Java syntax and is matched line by line. `^` and `$` are line anchors.
- **flags**: `i` ignores case. `m` matches against the whole file at once, for patterns that span lines. A finding is reported on the line where the match starts.

In `.yml` files the `code` and `strings` views are flattened, so each line reads as its full dotted key followed by its value. `^spring\.redis\.` therefore matches both `spring.redis.host=...` in a properties file and a `host:` line nested under `spring:` and `redis:` in YAML.

### What makes a good detector

- **Narrow.** It should be hard to construct correct code that it flags. Match on the import, the annotation or the distinctive call, not on a common word.
- **Unambiguous names only.** `new Timer(` could be anyone's class. Add `when-file` to require `java.util.Timer`.
- **Quiet when unsure.** A missed finding costs nothing. A wrong one sends an agent off to "fix" working code.
- **Honest status.** `removed` means it does not compile, does not start or is ignored on those versions. If it merely prints a warning, it is `deprecated`.

### The self-test

```sh
java skills/modern-java-guidance/scripts/LegacyScan.java selftest
```

It fails unless:

- every Before block is caught by this guide,
- every detector is triggered by at least one Before block,
- every After block is clean under **all** guides, with every version condition satisfied,
- the front matter is complete and well formed.

A guide may have several Before and After blocks in different languages. The fence language picks the file name the block is tested as: `java`, `xml` (`pom.xml`), `groovy` (`build.gradle`), `kotlin` (`build.gradle.kts`), `properties`, `yaml`, `dockerfile`, `sh`, `toml`. Override it with `file=`, for example ```` ```properties file=META-INF/spring.factories ````.

### Before you open the pull request

```sh
sh tests/run.sh                                                    # everything, including the self-test
java skills/modern-java-guidance/scripts/LegacyScan.java index     # regenerate INDEX.md
```

If the guide is about JDK APIs, add its After code to `examples/modern-jdk/ModernJdk.java`, which CI compiles with `-Xlint:deprecation,removal -Werror`. If it is about Spring Boot 4, consider showing it in `examples/modern-shop`. If it is about Spring AI, show it in `examples/modern-assistant`.

## Change the scanner

`LegacyScan.java` stays one file, at the Java 17 language level, with no dependencies. That constraint is the point: anyone with a JDK can run it, and an agent can read all of it.

- Rules do not go in the scanner. They go in guides.
- Add a test to `tests/run.sh` for any behavior you add or fix.
- The hook path must never fail a tool call for its own reasons. Any internal error exits 0.

## Report a false positive

Open an issue with the line that was flagged, the guide id, and your Java and Spring Boot versions. False positives are treated as bugs.

## License

By contributing you agree that your contribution is licensed under the Apache License 2.0.
