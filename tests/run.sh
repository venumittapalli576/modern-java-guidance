#!/bin/sh
# End-to-end tests for LegacyScan. Needs a JDK 17+ and git. Run from anywhere: sh tests/run.sh
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$ROOT/skills/modern-java-guidance/scripts/LegacyScan.java"
GUIDES="$ROOT/skills/modern-java-guidance/guides"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT
unset JAVA_TOOL_OPTIONS MODERN_JAVA_GUIDES MODERN_JAVA_HOOK_LEVEL

# Compile once so the suite is fast and so the Java 17 language level is enforced.
javac --release 17 -Xlint:all -Werror -d "$WORK/classes" "$SRC" || { echo "FAIL: does not compile at --release 17"; exit 1; }
scan() { java -cp "$WORK/classes" LegacyScan --guides "$GUIDES" "$@"; }

PASS=0; FAIL=0
ok()   { PASS=$((PASS + 1)); echo "ok    $1"; }
fail() { FAIL=$((FAIL + 1)); echo "FAIL  $1"; [ -n "${2:-}" ] && printf '%s\n' "$2" | sed 's/^/      /'; }
# expect <name> <expected exit> <grep pattern | - for any | SILENT for no output> <command...>
expect() {
    name=$1; want=$2; pattern=$3; shift 3
    out=$("$@" 2>&1); got=$?
    if [ "$got" != "$want" ]; then fail "$name (exit $got, wanted $want)" "$out"; return; fi
    if [ "$pattern" = "SILENT" ]; then
        if [ -n "$out" ]; then fail "$name (expected no output)" "$out"; return; fi
    elif [ "$pattern" != "-" ] && ! printf '%s' "$out" | grep -Eq -- "$pattern"; then fail "$name (output lacks: $pattern)" "$out"; return; fi
    ok "$name"
}
# refute <name> <grep pattern> <command...>   : the output must NOT contain the pattern
refute() {
    name=$1; pattern=$2; shift 2
    out=$("$@" 2>&1)
    if printf '%s' "$out" | grep -Eq -- "$pattern"; then fail "$name (output contains: $pattern)" "$out"; else ok "$name"; fi
}

boot_pom() { # boot_pom <dir> <boot version> <java version>
    mkdir -p "$1"
    cat > "$1/pom.xml" <<POM
<project>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>$2</version>
  </parent>
  <properties><java.version>$3</java.version></properties>
</project>
POM
}
legacy_java() { # a file with one pattern from several eras
    mkdir -p "$(dirname "$1")"
    cat > "$1" <<'JAVA'
package demo;

import javax.persistence.Entity;
import org.springframework.boot.test.mock.mockito.MockBean;

public class Legacy {
    private final RestTemplate rest = new RestTemplate();
    java.util.Date now = new java.util.Date();
    Object first(java.util.List<String> xs) { return xs.get(xs.size() - 1); }
}
JAVA
}

# ---------------------------------------------------------------- the guides themselves
expect "selftest: every guide proves its own examples" 0 "^OK: [0-9]+ guides" scan selftest
expect "index is up to date" 0 "up to date" scan index --check
expect "list prints guides" 0 "jakarta-ee-namespace" scan list
expect "list filters by category" 0 "security-filter-chain" scan list spring-security
expect "explain prints a guide" 0 "## After" scan explain rest-client
expect "explain rejects an unknown id" 64 "no guide with id" scan explain no-such-guide
expect "search finds by keyword" 0 "mockito-bean" scan search MockBean
expect "unknown option is a usage error" 64 "unknown option" scan scan --nope

# ---------------------------------------------------------------- version awareness
P="$WORK/boot40"; boot_pom "$P" 4.0.1 21; legacy_java "$P/src/main/java/demo/Legacy.java"
expect "boot 4.0: javax import is removed" 1 "removed +Import Jakarta EE" scan scan "$P"
expect "boot 4.0: @MockBean is removed" 1 "removed +Use @MockitoBean" scan scan "$P"
expect "boot 4.0: RestTemplate is only superseded" 1 "superseded +Use RestClient" scan scan "$P"
expect "boot 4.0: versions are read from the pom" 1 "Project: Java 21, Spring Boot 4.0 \[pom.xml\]" scan scan "$P"
expect "java 21: getLast is suggested" 1 "sequenced-collections" scan scan "$P"

P="$WORK/boot42"; boot_pom "$P" 4.2.0 25; legacy_java "$P/src/main/java/demo/Legacy.java"
expect "boot 4.2: RestTemplate is deprecated" 1 "deprecated +Use RestClient" scan scan "$P"

P="$WORK/boot35"; boot_pom "$P" 3.5.7 17; legacy_java "$P/src/main/java/demo/Legacy.java"
expect "boot 3.5: @MockBean is deprecated, not removed" 1 "deprecated +Use @MockitoBean" scan scan "$P"
refute "java 17: getLast is not suggested" "sequenced-collections" scan scan "$P"

P="$WORK/boot27"; boot_pom "$P" 2.7.18 11; legacy_java "$P/src/main/java/demo/Legacy.java"
refute "boot 2.7: javax imports are fine" "jakarta-ee-namespace" scan scan "$P"
refute "boot 2.7: @MockBean is fine" "mockito-bean" scan scan "$P"
refute "boot 2.7: RestClient does not exist yet" "rest-client" scan scan "$P"
expect "boot 2.7: java.time still applies" 0 "java-time" scan scan "$P"
expect "boot 2.7: nothing is removed, so exit 0" 0 "Carbon date: 2014" scan scan "$P"
expect "what-if: the same code against boot 4 and java 25" 1 "removed +Import Jakarta EE" scan scan "$P" --boot 4.0 --java 25

P="$WORK/gradle-kts"; mkdir -p "$P"; legacy_java "$P/src/main/java/demo/Legacy.java"
cat > "$P/build.gradle.kts" <<'KTS'
plugins {
    java
    id("org.springframework.boot") version "3.4.2"
}
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
KTS
expect "gradle kts: versions are detected" 1 "Project: Java 21, Spring Boot 3.4 \[build.gradle.kts\]" scan scan "$P"

P="$WORK/gradle-groovy"; mkdir -p "$P"; legacy_java "$P/src/main/java/demo/Legacy.java"
cat > "$P/build.gradle" <<'GROOVY'
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.5'
}
sourceCompatibility = '17'
GROOVY
expect "gradle groovy: versions are detected" 1 "Project: Java 17, Spring Boot 3.2 \[build.gradle\]" scan scan "$P"

P="$WORK/multi"; boot_pom "$P" 3.5.7 21; mkdir -p "$P/service"
printf '<project><parent><artifactId>multi</artifactId></parent></project>\n' > "$P/service/pom.xml"
legacy_java "$P/service/src/main/java/demo/Legacy.java"
expect "multi-module: versions come from the parent pom" 1 "Project: Java 21, Spring Boot 3.5" scan scan "$P/service"

P="$WORK/plain"; mkdir -p "$P"; legacy_java "$P/src/main/java/demo/Legacy.java"
printf '<project><properties><maven.compiler.release>21</maven.compiler.release></properties></project>\n' > "$P/pom.xml"
expect "no Spring Boot in the build: reported as such" 0 "Java 21, no Spring Boot" scan scan "$P"
refute "no Spring Boot in the build: Spring guides stay silent" "jakarta-ee-namespace|mockito-bean|rest-client" scan scan "$P"
expect "no Spring Boot in the build: JDK guides still apply" 0 "java-time" scan scan "$P"

P="$WORK/unknown-java"; mkdir -p "$P"; legacy_java "$P/src/main/java/demo/Legacy.java"
printf '<project><artifactId>x</artifactId></project>\n' > "$P/pom.xml"
expect "build without a Java level: 17 is assumed and said so" 0 "Java 17 \(assumed\)" scan scan "$P"

P="$WORK/pinned"; mkdir -p "$P"; legacy_java "$P/src/main/java/demo/Legacy.java"
printf '<project><artifactId>x</artifactId></project>\n' > "$P/pom.xml"
printf 'java=11\nboot=3.5\ndisable=java-time\n' > "$P/.modern-java"
expect ".modern-java pins the versions" 1 "Project: Java 11, Spring Boot 3.5 \[.modern-java\]" scan scan "$P"
refute ".modern-java disables a guide" "java-time" scan scan "$P"

P="$WORK/loose"; mkdir -p "$P"; legacy_java "$P/Legacy.java"
expect "loose file: current versions are assumed" 1 "Java 25 \(assumed\), Spring Boot 4.0 \(assumed\)" scan scan "$P/Legacy.java"

# ---------------------------------------------------------------- precision
P="$WORK/precise"; boot_pom "$P" 4.0.1 25; mkdir -p "$P/src/main/java/demo"
cat > "$P/src/main/java/demo/Clean.java" <<'JAVA'
package demo;

import javax.sql.DataSource;
import javax.crypto.Cipher;
import javax.annotation.processing.Generated;
import javax.transaction.xa.XAResource;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Clean {
    // new Date() and RestTemplate in a comment must not count
    String note = "WebSecurityConfigurerAdapter and javax.persistence in a string must not count";
    /* extends WebSecurityConfigurerAdapter */
    java.time.Instant now = java.time.Instant.now();
}
JAVA
expect "JDK javax packages, comments and strings are not flagged" 0 "no legacy patterns found" scan scan "$P"

cat > "$P/src/main/java/demo/Ignored.java" <<'JAVA'
package demo;

public class Ignored {
    java.util.Date a = new java.util.Date(); // modern-java:ignore java-time the vendor API needs a Date
    // modern-java:ignore
    java.util.Date b = new java.util.Date();
    java.util.Date c = new java.util.Date(); // modern-java:ignore rest-client (wrong guide, so still reported)
}
JAVA
expect "ignore comments silence the named guide only" 0 "1 finding in" scan scan "$P/src/main/java/demo/Ignored.java"

# ---------------------------------------------------------------- AI libraries
ai_project() { # ai_project <dir> <boot version>
    boot_pom "$1" "$2" 21; mkdir -p "$1/src/main/java/demo" "$1/src/main/resources"
    cat > "$1/src/main/java/demo/Assistant.java" <<'JAVA'
package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.function.FunctionCallback;

public class Assistant {
    String retired = "gpt-4";
    String current = "gpt-4o and gpt-4.1 and gpt-5-mini and claude-sonnet-4-5 are not retired ids";
    String bedrock = "anthropic.claude-3-sonnet-20240229-v1:0";
    String gateway = "openai/gpt-4";
    String azure = client.deploymentName("gpt-4");
    String gemini = "models/gemini-1.5-flash";
    Object memory = PromptChatMemoryAdvisor.builder(chatMemory).build();
}
JAVA
    cat > "$1/src/main/resources/application.yml" <<'YAML'
spring:
  ai:
    openai:
      chat:
        enabled: true
        options:
          model: gpt-3.5-turbo
    anthropic:
      chat:
        model: claude-sonnet-4-5
YAML
}
P="$WORK/ai2"; ai_project "$P" 4.1.2
expect "ai: a retired model id in a string is removed" 1 "removed +Do not hard-code a retired model id" scan scan "$P"
expect "ai: exactly the retired ids are reported, in Java and YAML" 1 "3 findings in" scan scan "$P" --only retired-model-ids
refute "ai: current ids, platform-prefixed ids and deployment names are left alone" "Assistant.java:(8|9|10|11):" scan scan "$P" --only retired-model-ids --format github
expect "ai: FunctionCallback is removed with Spring AI 2 (boot 4)" 1 "removed +Use @Tool and ToolCallback" scan scan "$P"
expect "ai: PromptChatMemoryAdvisor is removed with Spring AI 2 (boot 4)" 1 "spring-ai-message-chat-memory-advisor" scan scan "$P"
expect "ai: provider .enabled switches are removed" 1 "spring-ai-model-selection" scan scan "$P"
expect "ai: .options keys are deprecated with Spring AI 2" 1 "deprecated +Drop .options" scan scan "$P"

P="$WORK/ai1"; ai_project "$P" 3.5.7
expect "ai: FunctionCallback is only deprecated with Spring AI 1 (boot 3.5)" 1 "deprecated +Use @Tool and ToolCallback" scan scan "$P"
refute "ai: PromptChatMemoryAdvisor is fine with Spring AI 1" "spring-ai-message-chat-memory-advisor" scan scan "$P"
refute "ai: .options keys are current with Spring AI 1" "spring-ai-options-properties" scan scan "$P"

cat > "$WORK/Lc4j.java" <<'JAVA'
import dev.langchain4j.model.chat.ChatLanguageModel;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
class Lc4j { ChatLanguageModel model; }
JAVA
expect "ai: LangChain4j ChatLanguageModel is removed" 1 "langchain4j-chat-model" scan scan "$WORK/Lc4j.java"
expect "ai: the Vertex AI generativeai package is deprecated" 1 "google-genai-sdk" scan scan "$WORK/Lc4j.java"

expect "the modern examples are clean" 0 "no legacy patterns found" scan scan "$ROOT/examples/modern-shop" "$ROOT/examples/modern-assistant" "$ROOT/examples/modern-jdk" --java 25
expect "the legacy assistant is caught" 1 "12 removed, 3 deprecated" scan scan "$ROOT/examples/legacy-assistant"

# ---------------------------------------------------------------- non-Java files
P="$WORK/ops"; boot_pom "$P" 4.0.1 25; mkdir -p "$P/src/main/resources"
cat > "$P/Dockerfile" <<'DOCKER'
FROM openjdk:8-jdk-alpine
# -XX:+UseConcMarkSweepGC in a comment must not count
ENV JAVA_OPTS="-XX:MaxPermSize=256m -XX:+PrintGCDateStamps"
DOCKER
cat > "$P/src/main/resources/application.yml" <<'YAML'
spring:
  redis:
    host: cache
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
logging:
  file:
    name: app.log
YAML
expect "Dockerfile: deprecated base image" 1 "docker-base-image" scan scan "$P"
expect "Dockerfile: removed JVM flags" 1 "jvm-flags-removed" scan scan "$P"
expect "Dockerfile: removed GC logging flags" 1 "jvm-flags-gc-logging" scan scan "$P"
refute "Dockerfile: flags in comments are ignored" "jvm-flags-cms" scan scan "$P"
expect "YAML: nested keys are matched by their dotted path" 1 "boot3-config-properties" scan scan "$P"
expect "YAML: values are matched too" 1 "hibernate-dialect-autodetect" scan scan "$P"
refute "YAML: logging.file.name is not mistaken for logging.file" "boot2-config-properties" scan scan "$P"

# ---------------------------------------------------------------- output formats and exit codes
P="$WORK/boot40"
expect "--fail-on never always exits 0" 0 - scan scan "$P" --fail-on never
expect "--min removed hides lower statuses" 1 "removed" scan scan "$P" --min removed
refute "--min removed hides superseded" "superseded +Use" scan scan "$P" --min removed
expect "--only runs a single guide" 1 "1 finding in" scan scan "$P" --only jakarta-ee-namespace
expect "--disable skips a guide" 1 "findings in" scan scan "$P" --disable jakarta-ee-namespace
refute "--disable really skips it" "jakarta-ee-namespace" scan scan "$P" --disable jakarta-ee-namespace
expect "github format emits annotations" 1 "^::error file=.*title=jakarta-ee-namespace" scan scan "$P" --format github
JSON=$(scan scan "$P" --format json 2>/dev/null)
if command -v python3 >/dev/null 2>&1; then
    if printf '%s' "$JSON" | python3 -c 'import json,sys; d=json.load(sys.stdin); assert d["findings"] and all(k in d["findings"][0] for k in ("file","line","status","guide","title","note","source"))' 2>/dev/null; then ok "json output parses and has the documented fields"; else fail "json output parses and has the documented fields" "$JSON"; fi
fi

# ---------------------------------------------------------------- git: only changed lines
P="$WORK/repo"; boot_pom "$P" 4.0.1 25; mkdir -p "$P/src/main/java/demo"
cat > "$P/src/main/java/demo/Old.java" <<'JAVA'
package demo;

public class Old {
    java.util.Date created = new java.util.Date();
}
JAVA
( cd "$P" && git init -q . && git -c user.email=t@example.com -c user.name=t add -A && git -c user.email=t@example.com -c user.name=t commit -qm init )
expect "--changed: an untouched repo has nothing to report" 0 "no legacy patterns" scan scan "$P" --changed
cat >> "$P/src/main/java/demo/Old.java" <<'JAVA'
class Added { Object rest = new RestTemplate(); }
JAVA
expect "--changed: a new line is reported" 0 "rest-client" scan scan "$P" --changed
refute "--changed: old lines in the same file are not" "java-time" scan scan "$P" --changed
legacy_java "$P/src/main/java/demo/Fresh.java"
expect "--changed: untracked files are scanned whole" 1 "jakarta-ee-namespace" scan scan "$P" --changed

# ---------------------------------------------------------------- Claude Code hook
hook() { printf '%s' "$1" | java -cp "$WORK/classes" LegacyScan --guides "$GUIDES" hook; }
F="$P/src/main/java/demo/Old.java"
expect "hook: Edit reports only the text it inserted" 2 "1 legacy pattern.*Old.java" hook "{\"tool_name\":\"Edit\",\"tool_input\":{\"file_path\":\"$F\",\"old_string\":\"x\",\"new_string\":\"class Added { Object rest = new RestTemplate(); }\"}}"
expect "hook: the message names the guide file to read" 2 "guides/spring-web/rest-client.md" hook "{\"tool_name\":\"Edit\",\"tool_input\":{\"file_path\":\"$F\",\"old_string\":\"x\",\"new_string\":\"class Added { Object rest = new RestTemplate(); }\"}}"
expect "hook: an Edit that adds nothing legacy is silent" 0 SILENT hook "{\"tool_name\":\"Edit\",\"tool_input\":{\"file_path\":\"$F\",\"old_string\":\"x\",\"new_string\":\"public class Old {\"}}"
expect "hook: MultiEdit fragments are checked" 2 "rest-client" hook "{\"tool_name\":\"MultiEdit\",\"tool_input\":{\"file_path\":\"$F\",\"edits\":[{\"old_string\":\"a\",\"new_string\":\"package demo;\"},{\"old_string\":\"b\",\"new_string\":\"new RestTemplate()\"}]}}"
expect "hook: Write of a tracked file reports changed lines" 2 "1 legacy pattern" hook "{\"tool_name\":\"Write\",\"tool_input\":{\"file_path\":\"$F\",\"content\":\"...\"}}"
expect "hook: Write of a new file reports everything in it" 2 "jakarta-ee-namespace" hook "{\"tool_name\":\"Write\",\"tool_input\":{\"file_path\":\"$P/src/main/java/demo/Fresh.java\",\"content\":\"...\"}}"
expect "hook: files it has no guides for are ignored" 0 SILENT hook "{\"tool_name\":\"Write\",\"tool_input\":{\"file_path\":\"$ROOT/README.md\"}}"
expect "hook: a missing file is ignored" 0 SILENT hook "{\"tool_name\":\"Write\",\"tool_input\":{\"file_path\":\"$P/nope/Gone.java\"}}"
expect "hook: malformed input never breaks the agent" 0 SILENT hook "this is not json"
expect "hook: empty input never breaks the agent" 0 SILENT hook ""
MODERN_JAVA_HOOK_LEVEL=deprecated; export MODERN_JAVA_HOOK_LEVEL
expect "hook: MODERN_JAVA_HOOK_LEVEL raises the bar" 0 SILENT hook "{\"tool_name\":\"Edit\",\"tool_input\":{\"file_path\":\"$F\",\"old_string\":\"x\",\"new_string\":\"class Added { Object rest = new RestTemplate(); }\"}}"
unset MODERN_JAVA_HOOK_LEVEL
if XDG_CACHE_HOME="$WORK/cache" sh "$ROOT/hooks/post-edit.sh" < /dev/null; then ok "hook script: runs and stays silent on empty input"; else fail "hook script: runs and stays silent on empty input"; fi
out=$(printf '{"tool_name":"Write","tool_input":{"file_path":"%s"}}' "$P/src/main/java/demo/Fresh.java" | XDG_CACHE_HOME="$WORK/cache" sh "$ROOT/hooks/post-edit.sh" 2>&1); got=$?
if [ "$got" = 2 ] && printf '%s' "$out" | grep -q "jakarta-ee-namespace"; then ok "hook script: compiled cache path reports findings"; else fail "hook script: compiled cache path reports findings (exit $got)" "$out"; fi

# ---------------------------------------------------------------- source-launcher mode finds its own guides
expect "java LegacyScan.java works with no --guides" 0 "^OK:" java "$SRC" selftest

echo
echo "$PASS passed, $FAIL failed"
[ "$FAIL" = 0 ]
