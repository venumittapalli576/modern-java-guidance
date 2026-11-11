//JAVA 17+
/*
 * LegacyScan - finds legacy Java, Spring, build and JVM patterns that have a modern replacement.
 *
 * Part of modern-java-guidance. One file, no dependencies, runs on any JDK 17 or later:
 *
 *     java LegacyScan.java scan .
 *
 * The rules are not in this file. Every guide under ../guides/ carries its own detection
 * patterns in its front matter, so the text an agent reads and the check that enforces it
 * cannot drift apart. Run `java LegacyScan.java selftest` to prove that every guide's
 * "Before" example is caught and every "After" example is clean.
 *
 * Licensed under the Apache License, Version 2.0.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LegacyScan {

    static final String VERSION = "0.1.0";
    static final String IGNORE = "modern-java:ignore";
    static final String IGNORE_FILE = "modern-java:ignore-file";

    /** Assumed for a loose file with no build around it: the current LTS and Boot generation. */
    static final Version DEFAULT_JAVA = new Version(25, 0);
    static final Version DEFAULT_BOOT = new Version(4, 0);
    /** Assumed when a build exists but does not state its Java level: the lowest level current frameworks accept. */
    static final Version CONSERVATIVE_JAVA = new Version(17, 0);
    static final String CONFIG_FILE = ".modern-java";
    static final Version LATEST = new Version(999, 0);

    static final Set<String> SKIPPED_DIRS = Set.of(
            ".git", ".hg", ".svn", ".idea", ".vscode", ".gradle", ".mvn", "node_modules",
            "target", "build", "out", "dist", "bin", ".next", "generated", "generated-sources");

    // ------------------------------------------------------------------ model

    record Version(int major, int minor) implements Comparable<Version> {
        /** "3.5.7" -> 3.5, "4.0.0-RC1" -> 4.0, "17" -> 17.0. Returns null when there is no number. */
        static Version parse(String raw) {
            if (raw == null) return null;
            Matcher m = Pattern.compile("(\\d+)(?:[._](\\d+))?").matcher(raw);
            if (!m.find()) return null;
            int major = Integer.parseInt(m.group(1));
            int minor = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
            return new Version(major, minor);
        }

        /** Java versions: "1.8" and "VERSION_1_8" mean 8, "21.0.2" means 21. */
        static Version parseJava(String raw) {
            Version v = parse(raw);
            if (v == null) return null;
            return v.major == 1 ? new Version(v.minor, 0) : new Version(v.major, 0);
        }

        @Override public int compareTo(Version o) {
            return major != o.major ? Integer.compare(major, o.major) : Integer.compare(minor, o.minor);
        }

        String shortForm() { return minor == 0 ? String.valueOf(major) : major + "." + minor; }
        String dotted() { return major + "." + minor; }
    }

    /** Ordered from least to most severe. */
    enum Status {
        SUPERSEDED, HAZARD, DEPRECATED, REMOVED;

        String label() { return name().toLowerCase(Locale.ROOT); }

        static Status parse(String s) {
            try {
                return valueOf(s.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("unknown status '" + s + "' (expected superseded, hazard, deprecated or removed)");
            }
        }
    }

    enum Scope { CODE, STRINGS, COMMENTS, RAW }

    /** One clause such as {@code boot>=3.2} or {@code java<21}. */
    record Condition(String subject, boolean atLeast, Version version) {
        static List<Condition> parseAll(String raw) {
            List<Condition> out = new ArrayList<>();
            if (raw == null || raw.isBlank()) return out;
            for (String part : raw.split(",")) {
                Matcher m = Pattern.compile("^\\s*(java|boot)\\s*(>=|<)\\s*([\\d.]+)\\s*$").matcher(part);
                if (!m.matches()) throw new IllegalArgumentException("bad condition '" + part.trim() + "' (expected e.g. boot>=3.2 or java>=21)");
                out.add(new Condition(m.group(1), m.group(2).equals(">="), Version.parse(m.group(3))));
            }
            return out;
        }

        boolean test(Facts f) {
            Version actual = subject.equals("java") ? f.java() : f.boot();
            if (actual == null) return false;   // the project does not use Spring Boot
            int c = actual.compareTo(version);
            return atLeast ? c >= 0 : c < 0;
        }

        String describe() {
            String name = subject.equals("java") ? "Java " : "Spring Boot ";
            return name + (atLeast ? version.shortForm() + "+" : "before " + version.shortForm());
        }
    }

    record Detector(Scope scope, List<String> globs, List<Pattern> globPatterns, Pattern pattern, boolean wholeFile, String source) {
        boolean accepts(String unixPath) {
            String name = unixPath.substring(unixPath.lastIndexOf('/') + 1);
            for (int i = 0; i < globPatterns.size(); i++) {
                String subject = globs.get(i).contains("/") ? unixPath : name;
                if (globPatterns.get(i).matcher(subject).matches()) return true;
            }
            return false;
        }
    }

    record Example(String lang, String fileName, String code) {}

    record Guide(String id, String title, String category, Status kind, String note, String source,
                 int modernSince, List<Condition> applies, List<Condition> deprecated, List<Condition> removed,
                 List<Detector> detectors, Pattern whenFile, int limit, boolean adviceOnly,
                 String body, List<Example> before, List<Example> after, Path file) {

        boolean appliesTo(Facts f) { return applies.stream().allMatch(c -> c.test(f)); }

        Status statusFor(Facts f) {
            if (!removed.isEmpty() && removed.stream().allMatch(c -> c.test(f))) return Status.REMOVED;
            if (!deprecated.isEmpty() && deprecated.stream().allMatch(c -> c.test(f))
                    && kind.compareTo(Status.DEPRECATED) < 0) return Status.DEPRECATED;
            return kind;
        }

        String appliesText() {
            return applies.isEmpty() ? "any version" : applies.stream().map(Condition::describe).collect(Collectors.joining(", "));
        }

        /** The worst this guide can report, used for listings. */
        Status worst() {
            if (!removed.isEmpty()) return Status.REMOVED;
            if (!deprecated.isEmpty() && kind.compareTo(Status.DEPRECATED) < 0) return Status.DEPRECATED;
            return kind;
        }
    }

    /** @param boot null when the project has a build that does not use Spring Boot */
    record Facts(Version java, Version boot, boolean javaKnown, boolean bootKnown, String origin) {
        static Facts latest() { return new Facts(LATEST, LATEST, true, true, "selftest"); }

        String describe() {
            String j = "Java " + java.shortForm() + (javaKnown ? "" : " (assumed)");
            String b = boot == null ? "no Spring Boot" : "Spring Boot " + boot.dotted() + (bootKnown ? "" : " (assumed)");
            return j + ", " + b;
        }
    }

    /** @param more further hits of the same guide in the same file that a per-file limit left out */
    record Finding(Guide guide, Status status, Path file, int line, String excerpt, int more) {}

    record Views(String raw, String code, String strings, String comments) {}

    // ------------------------------------------------------------------ entry point

    public static void main(String[] argv) {
        int exit;
        try {
            exit = new LegacyScan().run(new ArrayList<>(Arrays.asList(argv)));
        } catch (UsageException e) {
            System.err.println("legacy-scan: " + e.getMessage());
            System.err.println("Run with --help for usage.");
            exit = 64;
        } catch (RuntimeException | IOException e) {
            System.err.println("legacy-scan: " + e);
            if (System.getenv("MODERN_JAVA_DEBUG") != null) e.printStackTrace();
            exit = 70;
        }
        System.out.flush();
        System.exit(exit);
    }

    static final class UsageException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        UsageException(String message) { super(message); }
    }

    // options
    Path guidesDir;
    Version forcedJava;
    Version forcedBoot;
    boolean forcedNoBoot;
    String format = "text";
    Status minStatus = Status.SUPERSEDED;
    Status failOn = Status.REMOVED;
    boolean neverFail;
    boolean changedOnly;
    String changedRef = "HEAD";
    boolean check;
    boolean verbose;
    final Set<String> disabled = ConcurrentHashMap.newKeySet();   // also filled from .modern-java files, in parallel
    Set<String> only = new LinkedHashSet<>();
    final Map<Path, Facts> factsCache = new ConcurrentHashMap<>();

    int run(List<String> args) throws IOException {
        if (args.isEmpty() || args.contains("--help") || args.contains("-h")) {
            System.out.print(USAGE);
            return 0;
        }
        if (args.contains("--version")) {
            System.out.println("legacy-scan " + VERSION);
            return 0;
        }
        List<String> positional = parseOptions(args);
        if (positional.isEmpty()) throw new UsageException("missing command");
        String command = positional.remove(0);
        if (command.equals("hook")) return hook();   // never loads anything it does not need, never throws

        List<Guide> guides = loadGuides(locateGuides());
        switch (command) {
            case "scan": return scan(guides, positional);
            case "list": return list(guides, positional);
            case "explain": return explain(guides, positional);
            case "search": return search(guides, positional);
            case "selftest": return selftest(guides);
            case "index": return index(guides);
            default: throw new UsageException("unknown command '" + command + "'");
        }
    }

    static final String USAGE = """
            legacy-scan %s - find legacy Java, Spring, build and JVM patterns

            Usage: java LegacyScan.java <command> [options]

            Commands
              scan [paths...]      Scan files or directories (default: current directory)
              list [category]      List every guide
              explain <id>         Print one guide in full
              search <words...>    Find guides by keyword
              selftest             Check every guide's Before example is caught and After is clean
              index [--check]      Regenerate INDEX.md from the guides (or fail if it is stale)
              hook                 Claude Code PostToolUse hook mode (reads the event on stdin)

            Scan options
              --java <n>           Target Java version (default: detected from the build, else %s)
              --boot <x.y|none>    Target Spring Boot version (default: detected, else %s)
              --changed [ref]      Only report lines changed since a git ref (default: HEAD) and untracked files
              --min <status>       Lowest status to report: superseded, hazard, deprecated, removed
              --fail-on <status>   Exit 1 when a finding at or above this status exists (default: removed; 'never' to disable)
              --only <ids>         Comma-separated guide ids to run
              --disable <ids>      Comma-separated guide ids to skip
              --format <f>         text (default), json or github
              --verbose            List every superseded finding instead of summarizing a long tail by guide
              --guides <dir>       Location of the guides directory

            Suppress one finding with a comment on the same or the previous line:
              // modern-java:ignore <guide-id> reason

            Pin versions the build does not reveal with a .modern-java file at the project root:
              java=21
              boot=3.5        (or boot=none)
              disable=switch-expressions,unnamed-variables
            """.formatted(VERSION, DEFAULT_JAVA.shortForm(), DEFAULT_BOOT.dotted());

    List<String> parseOptions(List<String> args) {
        List<String> positional = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            switch (a) {
                case "--guides": guidesDir = Path.of(value(args, ++i, a)); break;
                case "--java": forcedJava = requireVersion(Version.parseJava(value(args, ++i, a)), a); break;
                case "--boot":
                    String boot = value(args, ++i, a);
                    if (boot.equals("none")) forcedNoBoot = true; else forcedBoot = requireVersion(Version.parse(boot), a);
                    break;
                case "--format":
                    format = value(args, ++i, a);
                    if (!Set.of("text", "json", "github").contains(format)) throw new UsageException("unknown format '" + format + "'");
                    break;
                case "--min": minStatus = status(value(args, ++i, a)); break;
                case "--fail-on":
                    String v = value(args, ++i, a);
                    if (v.equals("never")) neverFail = true; else failOn = status(v);
                    break;
                case "--only": only.addAll(csv(value(args, ++i, a))); break;
                case "--disable": disabled.addAll(csv(value(args, ++i, a))); break;
                case "--check": check = true; break;
                case "--verbose": verbose = true; break;
                case "--changed":
                    changedOnly = true;
                    if (i + 1 < args.size() && !args.get(i + 1).startsWith("-") && !Files.exists(Path.of(args.get(i + 1)))) changedRef = args.get(++i);
                    break;
                default:
                    if (a.startsWith("--")) throw new UsageException("unknown option '" + a + "'");
                    positional.add(a);
            }
        }
        return positional;
    }

    static String value(List<String> args, int i, String option) {
        if (i >= args.size()) throw new UsageException(option + " needs a value");
        return args.get(i);
    }

    static Version requireVersion(Version v, String option) {
        if (v == null) throw new UsageException(option + " needs a version number");
        return v;
    }

    static Status status(String s) {
        try {
            return Status.parse(s);
        } catch (IllegalArgumentException e) {
            throw new UsageException(e.getMessage());
        }
    }

    static List<String> csv(String s) {
        return Arrays.stream(s.split(",")).map(String::trim).filter(x -> !x.isEmpty()).toList();
    }

    // ------------------------------------------------------------------ locating and loading guides

    Path locateGuides() {
        List<Path> candidates = new ArrayList<>();
        if (guidesDir != null) candidates.add(guidesDir);
        String env = System.getenv("MODERN_JAVA_GUIDES");
        if (env != null && !env.isBlank()) candidates.add(Path.of(env));
        Path self = selfLocation();
        if (self != null) {
            Path dir = Files.isDirectory(self) ? self : self.getParent();
            if (dir != null) {
                candidates.add(dir.resolveSibling("guides"));
                candidates.add(dir.resolve("guides"));
            }
        }
        candidates.add(Path.of("skills", "modern-java-guidance", "guides"));
        candidates.add(Path.of("guides"));
        for (Path c : candidates) {
            if (Files.isDirectory(c)) return c.toAbsolutePath().normalize();
        }
        throw new UsageException("cannot find the guides directory; pass --guides <dir> or set MODERN_JAVA_GUIDES");
    }

    /** Where this program lives: the .java file in source-launcher mode, else the class directory or jar. */
    static Path selfLocation() {
        try {
            URI uri = LegacyScan.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            if ("file".equals(uri.getScheme())) return Path.of(uri).toAbsolutePath();
        } catch (Exception ignored) {
            // fall through to the command line
        }
        String command = System.getProperty("sun.java.command", "");
        Matcher m = Pattern.compile("(?:^|\\s)(?:\\S*launcher\\.\\S+\\s+)?(.*?LegacyScan\\.java)(?:\\s|$)").matcher(command);
        if (m.find()) {
            Path p = Path.of(m.group(1)).toAbsolutePath();
            if (Files.exists(p)) return p;
        }
        return null;
    }

    static List<Guide> loadGuides(Path dir) throws IOException {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(dir)) {
            files = walk.filter(p -> p.toString().endsWith(".md")).sorted().toList();
        }
        List<Guide> guides = new ArrayList<>();
        Map<String, Path> seen = new HashMap<>();
        for (Path file : files) {
            Guide g;
            try {
                g = parseGuide(file, Files.readString(file));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(dir.relativize(file) + ": " + e.getMessage());
            }
            Path previous = seen.put(g.id(), file);
            if (previous != null) throw new IllegalArgumentException("duplicate guide id '" + g.id() + "' in " + previous + " and " + file);
            guides.add(g);
        }
        if (guides.isEmpty()) throw new IllegalArgumentException("no guides found in " + dir);
        guides.sort(Comparator.comparing(Guide::category).thenComparing(Guide::id));
        return guides;
    }

    static Guide parseGuide(Path file, String text) {
        text = text.replace("\r\n", "\n");
        if (!text.startsWith("---\n")) throw new IllegalArgumentException("missing front matter");
        int end = text.indexOf("\n---", 4);
        if (end < 0) throw new IllegalArgumentException("unterminated front matter");
        // The front matter is a small subset of YAML, so that it also renders as a table on GitHub:
        // "key: value" pairs, values optionally in single quotes, and one list ("detect:" followed by "  - item" lines).
        Map<String, List<String>> fm = new LinkedHashMap<>();
        String listKey = null;
        for (String line : text.substring(4, end).split("\n")) {
            if (line.isBlank() || line.startsWith("#")) continue;
            Matcher item = Pattern.compile("^\\s+-\\s+(.*)$").matcher(line);
            if (item.matches()) {
                if (listKey == null) throw new IllegalArgumentException("list item outside a list: " + line);
                fm.get(listKey).add(unquote(item.group(1).trim()));
                continue;
            }
            Matcher m = Pattern.compile("^([a-z][a-z-]*):\\s*(.*)$").matcher(line);
            if (!m.matches()) throw new IllegalArgumentException("bad front matter line: " + line);
            if (fm.containsKey(m.group(1))) throw new IllegalArgumentException("'" + m.group(1) + "' given more than once");
            String value = m.group(2).trim();
            List<String> values = fm.computeIfAbsent(m.group(1), k -> new ArrayList<>());
            if (value.isEmpty()) {
                listKey = m.group(1);
            } else {
                listKey = null;
                values.add(unquote(value));
            }
        }
        int bodyStart = text.indexOf('\n', end + 1);
        String body = bodyStart < 0 ? "" : text.substring(bodyStart + 1).strip() + "\n";

        String id = one(fm, "id", true);
        String stem = file.getFileName().toString().replaceFirst("\\.md$", "");
        if (!id.equals(stem)) throw new IllegalArgumentException("id '" + id + "' must match the file name");
        String category = one(fm, "category", true);
        if (file.getParent() != null && !file.getParent().getFileName().toString().equals(category)) {
            throw new IllegalArgumentException("category '" + category + "' must match the directory name");
        }
        String source = one(fm, "source", true);
        if (!source.startsWith("https://")) throw new IllegalArgumentException("source must be an https URL");
        int since;
        try {
            since = Integer.parseInt(one(fm, "modern-since", true));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("modern-since must be a year");
        }
        if (since < 1995 || since > Year.now().getValue()) throw new IllegalArgumentException("modern-since year out of range: " + since);

        List<Detector> detectors = new ArrayList<>();
        for (String d : fm.getOrDefault("detect", List.of())) detectors.add(parseDetector(d));
        boolean adviceOnly = "true".equals(one(fm, "advice-only", false));
        if (detectors.isEmpty() && !adviceOnly) throw new IllegalArgumentException("no detect lines (add 'advice-only: true' if that is intended)");
        if (!detectors.isEmpty() && adviceOnly) throw new IllegalArgumentException("advice-only guides cannot have detect lines");

        String when = one(fm, "when-file", false);
        String limit = one(fm, "limit", false);
        List<Example> before = new ArrayList<>();
        List<Example> after = new ArrayList<>();
        parseExamples(body, before, after);

        Set<String> known = Set.of("id", "title", "category", "kind", "note", "source", "modern-since",
                "applies", "deprecated", "removed", "detect", "when-file", "limit", "advice-only");
        for (String key : fm.keySet()) {
            if (!known.contains(key)) throw new IllegalArgumentException("unknown front matter key '" + key + "'");
        }
        return new Guide(id, one(fm, "title", true), category, Status.parse(one(fm, "kind", true)),
                one(fm, "note", true), source, since,
                Condition.parseAll(one(fm, "applies", false)),
                Condition.parseAll(one(fm, "deprecated", false)),
                Condition.parseAll(one(fm, "removed", false)),
                detectors, when == null ? null : compile(when, Pattern.MULTILINE),
                limit == null ? 0 : Integer.parseInt(limit), adviceOnly, body, before, after, file);
    }

    /** Reads a YAML plain or single-quoted scalar. Plain values that YAML would misread are rejected. */
    static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1).replace("''", "'");
        }
        if (value.matches("^[@`!&*%|>\\[{\"'#,?-].*") && !value.matches("^-?\\w.*") || value.contains(": ") || value.contains(" #")) {
            throw new IllegalArgumentException("put this value in single quotes so that it stays valid YAML: " + value);
        }
        return value;
    }

    static String one(Map<String, List<String>> fm, String key, boolean required) {
        List<String> values = fm.get(key);
        if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
            if (required) throw new IllegalArgumentException("missing '" + key + "'");
            return null;
        }
        if (values.size() > 1) throw new IllegalArgumentException("'" + key + "' given more than once");
        return values.get(0);
    }

    /** {@code <scope> <globs> /regex/flags}; flag i = ignore case, m = match across the whole file. */
    static Detector parseDetector(String raw) {
        Matcher m = Pattern.compile("^(code|strings|comments|raw)\\s+(\\S+)\\s+/(.*)/([im]*)$").matcher(raw);
        if (!m.matches()) throw new IllegalArgumentException("bad detect line (expected '<scope> <globs> /regex/'): " + raw);
        int flags = Pattern.MULTILINE;
        if (m.group(4).contains("i")) flags |= Pattern.CASE_INSENSITIVE;
        List<String> globs = csv(m.group(2));
        List<Pattern> globPatterns = globs.stream().map(LegacyScan::globToRegex).toList();
        return new Detector(Scope.valueOf(m.group(1).toUpperCase(Locale.ROOT)), globs, globPatterns,
                compile(m.group(3), flags), m.group(4).contains("m"), raw);
    }

    static Pattern compile(String regex, int flags) {
        try {
            return Pattern.compile(regex, flags);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("bad regex /" + regex + "/: " + e.getDescription());
        }
    }

    static Pattern globToRegex(String glob) {
        StringBuilder sb = new StringBuilder();
        if (glob.contains("/")) sb.append("(?:^|.*/)");
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            if (c == '*') {
                if (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
                    sb.append(".*");
                    i++;
                    if (i + 1 < glob.length() && glob.charAt(i + 1) == '/') i++;
                } else {
                    sb.append("[^/]*");
                }
            } else if (c == '?') {
                sb.append("[^/]");
            } else if ("\\.[]{}()+-^$|".indexOf(c) >= 0) {
                sb.append('\\').append(c);
            } else {
                sb.append(c);
            }
        }
        return Pattern.compile(sb.toString());
    }

    static final Map<String, String> EXAMPLE_FILES = Map.ofEntries(
            Map.entry("java", "Example.java"), Map.entry("xml", "pom.xml"), Map.entry("groovy", "build.gradle"),
            Map.entry("kotlin", "build.gradle.kts"), Map.entry("dockerfile", "Dockerfile"),
            Map.entry("properties", "application.properties"), Map.entry("yaml", "application.yml"),
            Map.entry("sh", "run.sh"), Map.entry("bash", "run.sh"), Map.entry("toml", "libs.versions.toml"));

    static void parseExamples(String body, List<Example> before, List<Example> after) {
        List<Example> target = null;
        String lang = null;
        String fileName = null;
        StringBuilder code = null;
        for (String line : body.split("\n", -1)) {
            if (code != null) {
                if (line.strip().equals("```")) {
                    if (target != null) target.add(new Example(lang, fileName, code.toString()));
                    code = null;
                } else {
                    code.append(line).append('\n');
                }
                continue;
            }
            if (line.startsWith("## ")) {
                String heading = line.substring(3).strip().toLowerCase(Locale.ROOT);
                target = heading.startsWith("before") ? before : heading.startsWith("after") ? after : null;
            } else if (line.startsWith("```")) {
                String info = line.substring(3).strip();
                String[] parts = info.split("\\s+");
                lang = parts[0].toLowerCase(Locale.ROOT);
                fileName = EXAMPLE_FILES.getOrDefault(lang, "example.txt");
                for (String p : parts) if (p.startsWith("file=")) fileName = p.substring(5);
                code = new StringBuilder();
            }
        }
    }

    // ------------------------------------------------------------------ project facts

    Facts factsFor(Path file) {
        Path dir = file.toAbsolutePath().normalize().getParent();
        if (dir == null) dir = Path.of("").toAbsolutePath();
        return factsCache.computeIfAbsent(dir, this::detectFacts);
    }

    Facts detectFacts(Path dir) {
        Version java = forcedJava;
        Version boot = forcedBoot;
        boolean noBoot = forcedNoBoot;
        boolean buildSeen = false;
        boolean bootMentioned = false;
        String origin = null;
        Path d = dir;
        for (int depth = 0; d != null && depth < 16; depth++, d = d.getParent()) {
            Path config = d.resolve(CONFIG_FILE);
            if (Files.isRegularFile(config)) {
                for (String line : readQuietly(config).split("\n")) {
                    Matcher m = Pattern.compile("^\\s*(java|boot|disable)\\s*=\\s*(\\S.*?)\\s*$").matcher(line);
                    if (!m.matches()) continue;
                    String value = m.group(2);
                    if (m.group(1).equals("java") && java == null) java = Version.parseJava(value);
                    if (m.group(1).equals("boot") && boot == null && !noBoot) {
                        if (value.equals("none")) noBoot = true; else boot = Version.parse(value);
                    }
                    if (m.group(1).equals("disable")) disabled.addAll(csv(value));
                }
                if (origin == null) origin = CONFIG_FILE;
            }
            for (String name : List.of("pom.xml", "build.gradle.kts", "build.gradle", "gradle/libs.versions.toml",
                    "gradle.properties", ".java-version", ".sdkmanrc", ".tool-versions")) {
                Path candidate = d.resolve(name);
                if (!Files.isRegularFile(candidate)) continue;
                String text = readQuietly(candidate);
                boolean isBuild = name.equals("pom.xml") || name.startsWith("build.gradle");
                if (isBuild) {
                    buildSeen = true;
                    if (text.contains("spring-boot") || text.contains("org.springframework.boot")) bootMentioned = true;
                }
                Version j = java == null ? detectJava(name, text) : null;
                Version b = boot == null && !noBoot ? detectBoot(name, text) : null;
                if (j != null) java = j;
                if (b != null) boot = b;
                if (origin == null && (j != null || b != null || isBuild)) origin = name;
            }
            if (Files.exists(d.resolve(".git"))) break;
        }
        if (forcedJava != null && (forcedBoot != null || forcedNoBoot)) origin = "set on the command line";
        boolean javaKnown = java != null;
        boolean bootKnown = boot != null || noBoot;
        if (java == null) java = buildSeen ? CONSERVATIVE_JAVA : DEFAULT_JAVA;
        if (boot == null && !noBoot) {
            // a build that never mentions Spring Boot is not a Spring Boot project: its guides stay silent
            if (buildSeen && !bootMentioned) bootKnown = true; else boot = DEFAULT_BOOT;
        }
        return new Facts(java, boot, javaKnown, bootKnown, origin == null ? "no build file" : origin);
    }

    static String readQuietly(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException | RuntimeException e) {
            return "";
        }
    }

    static Version detectJava(String fileName, String text) {
        String t = stripXmlComments(text);
        List<String> patterns;
        if (fileName.equals("pom.xml")) {
            patterns = List.of("<java\\.version>\\s*([\\d.]+)\\s*<", "<maven\\.compiler\\.release>\\s*(\\d+)\\s*<",
                    "<release>\\s*(\\d+)\\s*</release>", "<maven\\.compiler\\.(?:source|target)>\\s*([\\d.]+)\\s*<",
                    "<(?:source|target)>\\s*([\\d.]+)\\s*</(?:source|target)>");
        } else if (fileName.startsWith("build.gradle")) {
            patterns = List.of("JavaLanguageVersion\\.of\\(\\s*(\\d+)\\s*\\)", "jvmToolchain\\(\\s*(\\d+)\\s*\\)",
                    "(?:source|target)Compatibility\\s*=?\\s*(?:JavaVersion\\.VERSION_)?['\"]?([\\d._]+)",
                    "options\\.release\\s*(?:=|\\.set\\()\\s*(\\d+)");
        } else if (fileName.endsWith("libs.versions.toml")) {
            patterns = List.of("(?m)^\\s*(?:java|jdk|jvm)\\s*=\\s*\"([\\d.]+)\"");
        } else if (fileName.equals("gradle.properties")) {
            patterns = List.of("(?m)^\\s*(?:javaVersion|java\\.version|jdkVersion)\\s*=\\s*([\\d.]+)");
        } else if (fileName.equals(".java-version")) {
            patterns = List.of("^\\D*([\\d.]+)");
        } else if (fileName.equals(".sdkmanrc")) {
            patterns = List.of("(?m)^\\s*java\\s*=\\s*([\\d.]+)");
        } else if (fileName.equals(".tool-versions")) {
            patterns = List.of("(?m)^\\s*java\\s+\\D*([\\d.]+)");
        } else {
            return null;
        }
        for (String p : patterns) {
            Matcher m = Pattern.compile(p).matcher(t);
            if (m.find()) return Version.parseJava(m.group(1));
        }
        return null;
    }

    static Version detectBoot(String fileName, String text) {
        String t = stripXmlComments(text);
        List<String> patterns;
        if (fileName.equals("pom.xml")) {
            patterns = List.of(
                    "<parent>(?:(?!</parent>)[\\s\\S])*?<artifactId>\\s*spring-boot-starter-parent\\s*</artifactId>(?:(?!</parent>)[\\s\\S])*?<version>\\s*([\\d.]+)",
                    "<parent>(?:(?!</parent>)[\\s\\S])*?<version>\\s*([\\d.]+)[^<]*</version>(?:(?!</parent>)[\\s\\S])*?<artifactId>\\s*spring-boot-starter-parent\\s*</artifactId>",
                    "<spring-boot\\.version>\\s*([\\d.]+)",
                    "<artifactId>\\s*spring-boot-dependencies\\s*</artifactId>\\s*<version>\\s*([\\d.]+)");
        } else if (fileName.startsWith("build.gradle")) {
            patterns = List.of("org\\.springframework\\.boot['\"]?\\s*\\)?\\s*version\\s*\\(?\\s*['\"]([\\d.]+)",
                    "spring-boot-gradle-plugin:([\\d.]+)", "spring-boot-dependencies:([\\d.]+)",
                    "springBootVersion\\s*=\\s*['\"]([\\d.]+)");
        } else if (fileName.endsWith("libs.versions.toml")) {
            patterns = List.of("(?mi)^\\s*spring[-_]?boot\\s*=\\s*\"([\\d.]+)");
        } else if (fileName.equals("gradle.properties")) {
            patterns = List.of("(?mi)^\\s*spring[-_.]?boot[-_.]?version\\s*=\\s*([\\d.]+)");
        } else {
            return null;
        }
        for (String p : patterns) {
            Matcher m = Pattern.compile(p).matcher(t);
            if (m.find()) return Version.parse(m.group(1));
        }
        return null;
    }

    // ------------------------------------------------------------------ views of a file

    static Views viewsFor(String fileName, String raw) {
        raw = raw.replace("\r\n", "\n");
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".java") || lower.endsWith(".gradle") || lower.endsWith(".kts") || lower.endsWith(".kt")
                || lower.endsWith(".groovy") || lower.equals("jenkinsfile")) {
            return lexJavaLike(raw);
        }
        if (lower.endsWith(".xml")) {
            String stripped = stripXmlComments(raw);
            return new Views(raw, stripped, stripped, blankExcept(raw, stripped));
        }
        String stripped = stripHashComments(raw, lower.endsWith(".properties"));
        String comments = blankExcept(raw, stripped);
        if (lower.endsWith(".yml") || lower.endsWith(".yaml")) {
            String flat = flattenYaml(stripped);
            return new Views(raw, flat, flat, comments);
        }
        return new Views(raw, stripped, stripped, comments);
    }

    /** Splits Java-like source into code (strings blanked), code with strings, and comments only. Offsets are preserved. */
    static Views lexJavaLike(String s) {
        int n = s.length();
        StringBuilder code = new StringBuilder(n);
        StringBuilder strings = new StringBuilder(n);
        StringBuilder comments = new StringBuilder(n);
        final int NORMAL = 0, LINE = 1, BLOCK = 2, STR = 3, TEXT = 4, CHR = 5;
        int state = NORMAL;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            char next = i + 1 < n ? s.charAt(i + 1) : '\0';
            if (c == '\n') {
                code.append('\n');
                strings.append('\n');
                comments.append('\n');
                if (state == LINE || state == STR || state == CHR) state = NORMAL;
                continue;
            }
            switch (state) {
                case NORMAL:
                    if (c == '/' && next == '/') {
                        state = LINE;
                        emit(code, strings, comments, ' ', ' ', c);
                    } else if (c == '/' && next == '*') {
                        state = BLOCK;
                        emit(code, strings, comments, ' ', ' ', c);
                    } else if (c == '"' && s.startsWith("\"\"\"", i)) {
                        state = TEXT;
                        for (int k = 0; k < 3; k++) emit(code, strings, comments, '"', '"', ' ');
                        i += 2;
                    } else if (c == '"') {
                        state = STR;
                        emit(code, strings, comments, c, c, ' ');
                    } else if (c == '\'') {
                        state = CHR;
                        emit(code, strings, comments, c, c, ' ');
                    } else {
                        emit(code, strings, comments, c, c, ' ');
                    }
                    break;
                case LINE:
                    emit(code, strings, comments, ' ', ' ', c);
                    break;
                case BLOCK:
                    emit(code, strings, comments, ' ', ' ', c);
                    if (c == '*' && next == '/') {
                        emit(code, strings, comments, ' ', ' ', next);
                        i++;
                        state = NORMAL;
                    }
                    break;
                case STR:
                case CHR:
                    char quote = state == STR ? '"' : '\'';
                    if (c == '\\' && next != '\0' && next != '\n') {
                        emit(code, strings, comments, ' ', c, ' ');
                        emit(code, strings, comments, ' ', next, ' ');
                        i++;
                    } else if (c == quote) {
                        emit(code, strings, comments, c, c, ' ');
                        state = NORMAL;
                    } else {
                        emit(code, strings, comments, ' ', c, ' ');
                    }
                    break;
                default: // TEXT
                    if (c == '\\' && next != '\0' && next != '\n') {
                        emit(code, strings, comments, ' ', c, ' ');
                        emit(code, strings, comments, ' ', next, ' ');
                        i++;
                    } else if (c == '"' && s.startsWith("\"\"\"", i)) {
                        for (int k = 0; k < 3; k++) emit(code, strings, comments, '"', '"', ' ');
                        i += 2;
                        state = NORMAL;
                    } else {
                        emit(code, strings, comments, ' ', c, ' ');
                    }
            }
        }
        return new Views(s, code.toString(), strings.toString(), comments.toString());
    }

    static void emit(StringBuilder code, StringBuilder strings, StringBuilder comments, char c, char s, char k) {
        code.append(c);
        strings.append(s);
        comments.append(k);
    }

    static String stripXmlComments(String s) {
        Matcher m = Pattern.compile("<!--[\\s\\S]*?-->").matcher(s);
        StringBuilder sb = new StringBuilder(s);
        while (m.find()) {
            for (int i = m.start(); i < m.end(); i++) if (sb.charAt(i) != '\n') sb.setCharAt(i, ' ');
        }
        return sb.toString();
    }

    /** Blanks full-line comments, and trailing " #" comments except in .properties files where # can be data. */
    static String stripHashComments(String s, boolean fullLineOnly) {
        StringBuilder out = new StringBuilder(s.length());
        for (String line : s.split("\n", -1)) {
            String t = line.stripLeading();
            if (t.startsWith("#") || (fullLineOnly && t.startsWith("!"))) {
                out.append(" ".repeat(line.length()));
            } else if (!fullLineOnly) {
                int cut = -1;
                boolean single = false, dbl = false;
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == '\'' && !dbl) single = !single;
                    else if (c == '"' && !single) dbl = !dbl;
                    else if (c == '#' && !single && !dbl && i > 0 && Character.isWhitespace(line.charAt(i - 1))) {
                        cut = i;
                        break;
                    }
                }
                out.append(cut < 0 ? line : line.substring(0, cut) + " ".repeat(line.length() - cut));
            } else {
                out.append(line);
            }
            out.append('\n');
        }
        out.setLength(out.length() - 1);
        return out.toString();
    }

    /** Keeps only the characters that {@code stripped} blanked out, i.e. the comments. */
    static String blankExcept(String raw, String stripped) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char r = raw.charAt(i);
            sb.append(r == '\n' ? '\n' : (i < stripped.length() && stripped.charAt(i) == r ? ' ' : r));
        }
        return sb.toString();
    }

    /** Rewrites each YAML mapping line as its full dotted key, so one regex serves .properties and .yml alike. */
    static String flattenYaml(String s) {
        StringBuilder out = new StringBuilder(s.length() + 64);
        List<Integer> indents = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        Pattern entry = Pattern.compile("^(\\s*)((?:-\\s+)*)([\"']?)([\\w.\\-/\\[\\]]+)\\3\\s*:(?:\\s+(.*))?$");
        for (String line : s.split("\n", -1)) {
            String t = line.strip();
            if (t.isEmpty()) {
                out.append('\n');
                continue;
            }
            if (t.startsWith("---") || t.startsWith("...")) {
                indents.clear();
                keys.clear();
                out.append('\n');
                continue;
            }
            Matcher m = entry.matcher(line);
            if (!m.matches()) {
                out.append(t).append('\n');
                continue;
            }
            int indent = m.group(1).length() + m.group(2).length();
            while (!indents.isEmpty() && indents.get(indents.size() - 1) >= indent) {
                indents.remove(indents.size() - 1);
                keys.remove(keys.size() - 1);
            }
            indents.add(indent);
            keys.add(m.group(4));
            out.append(String.join(".", keys)).append(':');
            if (m.group(5) != null && !m.group(5).isBlank()) out.append(' ').append(m.group(5).strip());
            out.append('\n');
        }
        out.setLength(out.length() - 1);
        return out.toString();
    }

    // ------------------------------------------------------------------ scanning

    static String unix(Path p) { return p.toString().replace('\\', '/'); }

    static boolean interesting(String unixPath, Collection<Guide> guides) {
        for (Guide g : guides) for (Detector d : g.detectors()) if (d.accepts(unixPath)) return true;
        return false;
    }

    /** @param onlyLines when non-null, findings on other lines are dropped (1-based line numbers) */
    List<Finding> scanText(Path file, String text, Facts facts, Collection<Guide> guides, BitSet onlyLines) {
        String path = unix(file);
        Views views = null;
        String[] rawLines = null;
        String[][] linesByScope = new String[Scope.values().length][];
        List<Finding> findings = new ArrayList<>();
        Set<String> guideIds = null;
        for (Guide g : guides) {
            if (g.detectors().isEmpty() || disabled.contains(g.id())) continue;
            if (!only.isEmpty() && !only.contains(g.id())) continue;
            if (!g.appliesTo(facts)) continue;
            Status status = g.statusFor(facts);
            if (status.compareTo(minStatus) < 0) continue;
            TreeMap<Integer, String> hits = new TreeMap<>();
            for (Detector d : g.detectors()) {
                if (!d.accepts(path)) continue;
                if (views == null) {
                    views = viewsFor(file.getFileName().toString(), text);
                    rawLines = views.raw().split("\n", -1);
                }
                String view = switch (d.scope()) {
                    case CODE -> views.code();
                    case STRINGS -> views.strings();
                    case COMMENTS -> views.comments();
                    case RAW -> views.raw();
                };
                Matcher whole = d.pattern().matcher(view);
                if (d.wholeFile()) {
                    while (whole.find()) {
                        hits.putIfAbsent(lineOf(view, firstNonBlank(view, whole.start(), whole.end())), "");
                        if (whole.end() == whole.start() && whole.end() >= view.length()) break;
                    }
                } else {
                    // One pass over the whole view rules most detectors out before any per-line work.
                    if (!whole.find()) continue;
                    int scope = d.scope().ordinal();
                    if (linesByScope[scope] == null) linesByScope[scope] = view.split("\n", -1);
                    String[] lines = linesByScope[scope];
                    Matcher m = d.pattern().matcher("");
                    for (int i = 0; i < lines.length; i++) {
                        if (m.reset(lines[i]).find()) hits.putIfAbsent(i + 1, "");
                    }
                }
            }
            if (hits.isEmpty()) continue;
            if (g.whenFile() != null && !g.whenFile().matcher(views.code()).find()) continue;
            if (guideIds == null) guideIds = guides.stream().map(Guide::id).collect(Collectors.toSet());
            if (fileIgnores(rawLines, g.id(), guideIds)) continue;
            int reported = 0;
            int suppressed = 0;
            for (int line : hits.keySet()) {
                if (onlyLines != null && !onlyLines.get(line)) continue;
                if (lineIgnores(rawLines, line, g.id(), guideIds)) continue;
                if (g.limit() > 0 && reported >= g.limit()) {
                    suppressed++;
                    continue;
                }
                String excerpt = line - 1 < rawLines.length ? rawLines[line - 1].strip() : "";
                findings.add(new Finding(g, status, file, line, excerpt, 0));
                reported++;
            }
            if (suppressed > 0) {
                Finding last = findings.remove(findings.size() - 1);
                findings.add(new Finding(last.guide(), last.status(), last.file(), last.line(), last.excerpt(), suppressed));
            }
        }
        findings.sort(Comparator.comparingInt(Finding::line).thenComparing(f -> f.guide().id()));
        return findings;
    }

    /** A multi-line match may begin with the blank lines before a statement; report where its text starts. */
    static int firstNonBlank(String text, int from, int to) {
        for (int i = from; i < to && i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) return i;
        }
        return from;
    }

    static int lineOf(String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < text.length(); i++) if (text.charAt(i) == '\n') line++;
        return line;
    }

    static boolean lineIgnores(String[] rawLines, int line, String id, Set<String> guideIds) {
        return ignores(rawLines, line - 1, IGNORE, id, guideIds) || ignores(rawLines, line - 2, IGNORE, id, guideIds);
    }

    static boolean fileIgnores(String[] rawLines, String id, Set<String> guideIds) {
        for (int i = 0; i < rawLines.length && i < 40; i++) if (ignores(rawLines, i, IGNORE_FILE, id, guideIds)) return true;
        return false;
    }

    static boolean ignores(String[] rawLines, int index, String marker, String id, Set<String> guideIds) {
        if (index < 0 || index >= rawLines.length) return false;
        String line = rawLines[index];
        int at = line.indexOf(marker);
        if (at < 0) return false;
        String rest = line.substring(at + marker.length());
        if (marker.equals(IGNORE) && rest.startsWith("-file")) return false;
        Matcher m = Pattern.compile("[a-z0-9][a-z0-9-]*").matcher(rest);
        boolean named = false;
        while (m.find()) {
            if (!guideIds.contains(m.group())) continue;
            named = true;
            if (m.group().equals(id)) return true;
        }
        return !named;   // a bare marker silences every guide
    }

    List<Path> collectFiles(List<String> roots, Collection<Guide> guides) throws IOException {
        List<Path> files = new ArrayList<>();
        for (String root : roots) {
            Path p = Path.of(root);
            if (!Files.exists(p)) throw new UsageException("no such file or directory: " + root);
            if (Files.isRegularFile(p)) {
                files.add(p);
                continue;
            }
            Files.walkFileTree(p, new SimpleFileVisitor<>() {
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
                    return !dir.equals(p) && SKIPPED_DIRS.contains(name) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }

                @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && attrs.size() < 2_000_000 && interesting(unix(file), guides)) files.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        files.sort(Comparator.comparing(LegacyScan::unix));
        return files;
    }

    int scan(List<Guide> guides, List<String> roots) throws IOException {
        if (roots.isEmpty()) roots = List.of(".");
        for (String id : only) requireGuide(guides, id);
        for (String id : disabled) requireGuide(guides, id);
        List<Path> files = collectFiles(roots, guides);
        Map<Path, BitSet> changed = null;
        if (changedOnly) {
            changed = gitChangedLines(Path.of(roots.get(0)), changedRef);
            if (changed == null) throw new UsageException("--changed needs a git repository");
        }
        record Scanned(Path file, Facts facts, List<Finding> findings) {}
        final Map<Path, BitSet> changedLines = changed;
        List<Scanned> results = files.parallelStream().map(file -> {
            BitSet lines = null;
            if (changedLines != null) {
                Path key = file.toAbsolutePath().normalize();
                if (!changedLines.containsKey(key)) return null;
                lines = changedLines.get(key);   // a null value means the whole file is new
            }
            String text;
            try {
                text = Files.readString(file);
            } catch (IOException | RuntimeException e) {
                return null;   // binary or unreadable
            }
            Facts facts = factsFor(file);
            return new Scanned(file, facts, scanText(file, text, facts, guides, lines));
        }).filter(r -> r != null).toList();
        List<Finding> findings = new ArrayList<>();
        Set<String> projects = new LinkedHashSet<>();
        int scanned = results.size();
        for (Scanned r : results) {
            projects.add(r.facts().describe() + " [" + r.facts().origin() + "]");
            findings.addAll(r.findings());
        }
        switch (format) {
            case "json": reportJson(findings, scanned); break;
            case "github": reportGithub(findings); break;
            default: reportText(findings, scanned, projects);
        }
        if (neverFail) return 0;
        return findings.stream().anyMatch(f -> f.status().compareTo(failOn) >= 0) ? 1 : 0;
    }

    static Guide requireGuide(List<Guide> guides, String id) {
        return guides.stream().filter(g -> g.id().equals(id)).findFirst()
                .orElseThrow(() -> new UsageException("no guide with id '" + id + "' (see: list)"));
    }

    // ------------------------------------------------------------------ git

    /** Added or modified lines per file since {@code ref}; a null value marks an untracked (wholly new) file. */
    static Map<Path, BitSet> gitChangedLines(Path anyPathInRepo, String ref) {
        Path start = Files.isDirectory(anyPathInRepo) ? anyPathInRepo : anyPathInRepo.toAbsolutePath().getParent();
        String top = git(start, "rev-parse", "--show-toplevel");
        if (top == null || top.isBlank()) return null;
        Path root = Path.of(top.strip());
        Map<Path, BitSet> changed = new HashMap<>();
        String diff = git(root, "diff", "-U0", "--no-color", "--no-ext-diff", ref, "--");
        if (diff == null) diff = git(root, "diff", "-U0", "--no-color", "--no-ext-diff", "--");   // no commits yet
        if (diff != null) {
            BitSet current = null;
            Pattern hunk = Pattern.compile("^@@ -\\d+(?:,\\d+)? \\+(\\d+)(?:,(\\d+))? @@");
            for (String line : diff.split("\n")) {
                if (line.startsWith("+++ ")) {
                    String name = line.substring(4).strip();
                    current = null;
                    if (name.startsWith("b/")) {
                        current = new BitSet();
                        changed.put(root.resolve(name.substring(2)).toAbsolutePath().normalize(), current);
                    }
                } else if (current != null) {
                    Matcher m = hunk.matcher(line);
                    if (m.find()) {
                        int from = Integer.parseInt(m.group(1));
                        int count = m.group(2) == null ? 1 : Integer.parseInt(m.group(2));
                        if (count > 0) current.set(from, from + count);
                    }
                }
            }
        }
        String untracked = git(root, "ls-files", "--others", "--exclude-standard");
        if (untracked != null) {
            for (String name : untracked.split("\n")) {
                if (!name.isBlank()) changed.put(root.resolve(name.strip()).toAbsolutePath().normalize(), null);
            }
        }
        return changed;
    }

    static String git(Path dir, String... args) {
        List<String> command = new ArrayList<>(List.of("git", "-C", dir.toString()));
        command.addAll(Arrays.asList(args));
        try {
            Process p = new ProcessBuilder(command).redirectErrorStream(false).start();
            p.getOutputStream().close();
            byte[] out;
            try (InputStream in = p.getInputStream()) {
                out = in.readAllBytes();
            }
            if (!p.waitFor(20, TimeUnit.SECONDS)) {
                p.destroyForcibly();
                return null;
            }
            return p.exitValue() == 0 ? new String(out, StandardCharsets.UTF_8) : null;
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    // ------------------------------------------------------------------ reporting

    static boolean color() {
        return System.console() != null && System.getenv("NO_COLOR") == null && !"dumb".equals(System.getenv("TERM"));
    }

    static String paint(Status s, String text) {
        if (!color()) return text;
        String code = switch (s) {
            case REMOVED -> "31;1";
            case DEPRECATED -> "33;1";
            case HAZARD -> "35;1";
            case SUPERSEDED -> "36";
        };
        return "\u001B[" + code + "m" + text + "\u001B[0m";
    }

    static String dim(String text) { return color() ? "\u001B[2m" + text + "\u001B[0m" : text; }

    static String bold(String text) { return color() ? "\u001B[1m" + text + "\u001B[0m" : text; }

    /** Above this many, superseded findings are summarized per guide so a large codebase still fits one screen. */
    static final int SUPERSEDED_DETAIL_LIMIT = 40;

    void reportText(List<Finding> all, int scanned, Set<String> projects) {
        PrintStream out = System.out;
        Path cwd = Path.of("").toAbsolutePath();
        long supersededCount = all.stream().filter(f -> f.status() == Status.SUPERSEDED).count();
        boolean summarize = !verbose && supersededCount > SUPERSEDED_DETAIL_LIMIT;
        List<Finding> detailed = summarize ? all.stream().filter(f -> f.status() != Status.SUPERSEDED).toList() : all;

        Map<Path, List<Finding>> byFile = new LinkedHashMap<>();
        for (Finding f : detailed) byFile.computeIfAbsent(f.file(), k -> new ArrayList<>()).add(f);
        Set<String> explained = new LinkedHashSet<>();
        for (Map.Entry<Path, List<Finding>> e : byFile.entrySet()) {
            out.println(bold(display(cwd, e.getKey())));
            for (Finding f : e.getValue()) {
                // Kept within about 118 columns: the guide id moves to its own line when the title is long,
                // and the explanation is printed the first time a guide appears, not on every line.
                String indent = " ".repeat(21);
                String suffix = "[" + f.guide().id() + "]" + (f.more() > 0 ? " (+" + f.more() + " more in this file)" : "");
                List<String> title = wrap(f.guide().title(), 96);
                boolean inline = title.get(title.size() - 1).length() + 2 + suffix.length() <= 97;
                for (int i = 0; i < title.size(); i++) {
                    String head = i == 0 ? String.format("  %5d  %s  ", f.line(), paint(f.status(), pad(f.status().label(), 10))) : indent;
                    out.println(head + title.get(i) + (inline && i == title.size() - 1 ? "  " + dim(suffix) : ""));
                }
                if (!inline) out.println(indent + dim(suffix));
                if (explained.add(f.guide().id())) {
                    for (String line : wrap(f.guide().note(), 96)) out.println(indent + dim(line));
                }
            }
            out.println();
        }
        if (summarize) {
            Map<Guide, int[]> perGuide = new LinkedHashMap<>();
            for (Finding f : all) {
                if (f.status() == Status.SUPERSEDED) perGuide.computeIfAbsent(f.guide(), k -> new int[1])[0]++;
            }
            out.println(bold("Superseded patterns, by guide") + dim("  (list one with --only <id>, or all with --verbose)"));
            perGuide.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                    .forEach(e -> out.printf("  %5d  %s  %s%n", e.getValue()[0], paint(Status.SUPERSEDED, pad(e.getKey().id(), 30)), e.getKey().title()));
            out.println();
        }
        for (String p : projects) out.println(dim("Project: " + p));
        if (all.isEmpty()) {
            out.println(scanned + " file" + (scanned == 1 ? "" : "s") + " scanned, no legacy patterns found.");
            out.println("Carbon date: current.");
            return;
        }
        Map<Status, Long> counts = all.stream().collect(Collectors.groupingBy(Finding::status, TreeMap::new, Collectors.counting()));
        List<String> parts = new ArrayList<>();
        for (Status s : List.of(Status.REMOVED, Status.DEPRECATED, Status.HAZARD, Status.SUPERSEDED)) {
            if (counts.containsKey(s)) parts.add(paint(s, counts.get(s) + " " + s.label()));
        }
        long files = all.stream().map(Finding::file).distinct().count();
        out.println(bold(all.size() + " finding" + (all.size() == 1 ? "" : "s")) + " in " + files + " of "
                + scanned + " file" + (scanned == 1 ? "" : "s") + ": " + String.join(", ", parts));
        Finding oldest = all.stream().min(Comparator.comparingInt((Finding f) -> f.guide().modernSince())
                .thenComparing(f -> f.guide().id())).orElseThrow();
        int years = Year.now().getValue() - oldest.guide().modernSince();
        out.println(bold("Carbon date: " + oldest.guide().modernSince()) + ". The oldest pattern here (" + oldest.guide().id()
                + ") has had a standard replacement for " + years + " year" + (years == 1 ? "" : "s") + ".");
        out.println(dim("Read a guide: java LegacyScan.java explain <id>"));
    }

    static String display(Path cwd, Path file) {
        Path abs = file.toAbsolutePath().normalize();
        return unix(abs.startsWith(cwd) ? cwd.relativize(abs) : file);
    }

    static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > width) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    static String pad(String s, int width) { return s.length() >= width ? s : s + " ".repeat(width - s.length()); }

    void reportJson(List<Finding> findings, int scanned) {
        Path cwd = Path.of("").toAbsolutePath();
        StringBuilder sb = new StringBuilder("{\"version\":\"" + VERSION + "\",\"filesScanned\":" + scanned + ",\"findings\":[");
        for (int i = 0; i < findings.size(); i++) {
            Finding f = findings.get(i);
            if (i > 0) sb.append(',');
            sb.append("\n  {\"file\":").append(json(display(cwd, f.file())))
                    .append(",\"line\":").append(f.line())
                    .append(",\"status\":").append(json(f.status().label()))
                    .append(",\"guide\":").append(json(f.guide().id()))
                    .append(",\"title\":").append(json(f.guide().title()))
                    .append(",\"note\":").append(json(f.guide().note()))
                    .append(",\"modernSince\":").append(f.guide().modernSince())
                    .append(",\"source\":").append(json(f.guide().source()))
                    .append(",\"excerpt\":").append(json(f.excerpt()))
                    .append(",\"moreInFile\":").append(f.more()).append('}');
        }
        sb.append(findings.isEmpty() ? "]}" : "\n]}");
        System.out.println(sb);
    }

    void reportGithub(List<Finding> findings) {
        Path cwd = Path.of("").toAbsolutePath();
        for (Finding f : findings) {
            String level = f.status() == Status.REMOVED ? "error" : f.status() == Status.SUPERSEDED ? "notice" : "warning";
            String message = (f.guide().title() + ". " + f.guide().note()).replace("%", "%25").replace("\r", "").replace("\n", "%0A");
            System.out.printf("::%s file=%s,line=%d,title=%s (%s)::%s%n", level, display(cwd, f.file()), f.line(),
                    f.guide().id(), f.status().label(), message);
        }
        System.out.println(findings.size() + " legacy pattern" + (findings.size() == 1 ? "" : "s") + " found.");
    }

    static String json(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c)); else sb.append(c);
            }
        }
        return sb.append('"').toString();
    }

    // ------------------------------------------------------------------ list, explain, search, index

    int list(List<Guide> guides, List<String> args) {
        String category = args.isEmpty() ? null : args.get(0);
        String current = null;
        int shown = 0;
        for (Guide g : guides) {
            if (category != null && !g.category().equals(category)) continue;
            if (!g.category().equals(current)) {
                current = g.category();
                System.out.println((shown == 0 ? "" : "\n") + bold(current));
            }
            System.out.printf("  %-38s %s  %s%n", g.id(), paint(g.worst(), pad(g.worst().label(), 10)), g.title());
            shown++;
        }
        if (shown == 0) throw new UsageException("no guides in category '" + category + "'");
        System.out.println("\n" + shown + " guide" + (shown == 1 ? "" : "s") + ". Read one: java LegacyScan.java explain <id>");
        return 0;
    }

    int explain(List<Guide> guides, List<String> args) {
        if (args.isEmpty()) throw new UsageException("explain needs a guide id");
        for (String id : args) {
            Guide g = requireGuide(guides, id);
            System.out.println("# " + g.title());
            System.out.println();
            System.out.println("- Guide: " + g.id() + " (" + g.category() + ")");
            System.out.println("- Status: " + statusLine(g));
            System.out.println("- Applies to: " + g.appliesText());
            System.out.println("- Replacement available since: " + g.modernSince());
            System.out.println("- Source: " + g.source());
            System.out.println();
            System.out.println(g.note());
            System.out.println();
            System.out.print(g.body());
            if (args.size() > 1) System.out.println("\n---\n");
        }
        return 0;
    }

    static String statusLine(Guide g) {
        List<String> parts = new ArrayList<>();
        parts.add(g.kind().label());
        if (!g.deprecated().isEmpty() && g.kind().compareTo(Status.DEPRECATED) < 0) {
            parts.add("deprecated on " + g.deprecated().stream().map(Condition::describe).collect(Collectors.joining(", ")));
        }
        if (!g.removed().isEmpty()) {
            parts.add("removed on " + g.removed().stream().map(Condition::describe).collect(Collectors.joining(", ")));
        }
        return String.join("; ", parts);
    }

    int search(List<Guide> guides, List<String> words) {
        if (words.isEmpty()) throw new UsageException("search needs at least one word");
        record Hit(Guide guide, int score) {}
        List<Hit> hits = new ArrayList<>();
        for (Guide g : guides) {
            String head = (g.id() + " " + g.title() + " " + g.category()).toLowerCase(Locale.ROOT);
            String rest = (g.note() + " " + g.body()).toLowerCase(Locale.ROOT);
            int score = 0;
            for (String w : words) {
                String needle = w.toLowerCase(Locale.ROOT);
                if (head.contains(needle)) score += 5;
                if (rest.contains(needle)) score += 1;
            }
            if (score > 0) hits.add(new Hit(g, score));
        }
        hits.sort(Comparator.comparingInt(Hit::score).reversed().thenComparing(h -> h.guide().id()));
        if (hits.isEmpty()) {
            System.out.println("No guide matches. Try: java LegacyScan.java list");
            return 1;
        }
        for (Hit h : hits.stream().limit(12).toList()) {
            System.out.printf("  %-38s %s%n", h.guide().id(), h.guide().title());
        }
        return 0;
    }

    int index(List<Guide> guides) throws IOException {
        Path dir = guides.get(0).file().getParent().getParent();
        Path target = dir.resolveSibling("INDEX.md");
        StringBuilder sb = new StringBuilder();
        sb.append("# Guide index\n\n");
        sb.append("Generated by `java scripts/LegacyScan.java index`. Do not edit by hand.\n\n");
        sb.append(guides.size()).append(" guides. Each lives at `guides/<category>/<id>.md`.\n");
        String current = null;
        for (Guide g : guides) {
            if (!g.category().equals(current)) {
                current = g.category();
                sb.append("\n## ").append(current).append("\n\n");
                sb.append("| Guide | Status | Applies to | Summary |\n|---|---|---|---|\n");
            }
            sb.append("| [`").append(g.id()).append("`](guides/").append(g.category()).append('/').append(g.id()).append(".md) | ")
                    .append(statusLine(g)).append(" | ").append(g.appliesText()).append(" | ")
                    .append(g.title().replace("|", "\\|")).append(g.adviceOnly() ? " (advice only, not scanned)" : "").append(" |\n");
        }
        String content = sb.toString();
        if (check) {
            String existing = Files.exists(target) ? Files.readString(target).replace("\r\n", "\n") : "";
            if (!existing.equals(content)) {
                System.err.println("INDEX.md is stale. Run: java LegacyScan.java index");
                return 1;
            }
            System.out.println("INDEX.md is up to date (" + guides.size() + " guides).");
            return 0;
        }
        Files.writeString(target, content);
        System.out.println("Wrote " + target + " (" + guides.size() + " guides).");
        return 0;
    }

    // ------------------------------------------------------------------ selftest

    int selftest(List<Guide> guides) {
        Facts latest = Facts.latest();
        List<String> failures = new ArrayList<>();
        int examples = 0;
        for (Guide g : guides) {
            if (g.adviceOnly()) {
                if (!g.before().isEmpty() || g.after().isEmpty()) failures.add(g.id() + ": advice-only guides need an After example and no Before example");
            } else {
                if (g.before().isEmpty()) failures.add(g.id() + ": no Before example");
                if (g.after().isEmpty()) failures.add(g.id() + ": no After example");
            }
            if (g.note().length() > 220) failures.add(g.id() + ": note is longer than 220 characters");
            Set<Detector> proven = new LinkedHashSet<>();
            for (Example ex : g.before()) {
                examples++;
                Path file = Path.of(ex.fileName());
                if (scanText(file, ex.code(), latest, List.of(g), null).isEmpty()) {
                    failures.add(g.id() + ": Before example (" + ex.fileName() + ") is not detected");
                }
                for (Detector d : g.detectors()) {
                    Guide single = new Guide(g.id(), g.title(), g.category(), g.kind(), g.note(), g.source(), g.modernSince(),
                            g.applies(), g.deprecated(), g.removed(), List.of(d), g.whenFile(), 0, false, g.body(),
                            g.before(), g.after(), g.file());
                    if (!scanText(file, ex.code(), latest, List.of(single), null).isEmpty()) proven.add(d);
                }
            }
            for (Detector d : g.detectors()) {
                if (!proven.contains(d)) failures.add(g.id() + ": no Before example triggers detector: " + d.source());
            }
            for (Example ex : g.after()) {
                examples++;
                for (Finding f : scanText(Path.of(ex.fileName()), ex.code(), latest, guides, null)) {
                    failures.add(g.id() + ": After example (" + ex.fileName() + ") line " + f.line() + " is flagged by '"
                            + f.guide().id() + "': " + f.excerpt());
                }
            }
        }
        if (!failures.isEmpty()) {
            failures.forEach(f -> System.err.println("FAIL " + f));
            System.err.println(failures.size() + " failure" + (failures.size() == 1 ? "" : "s") + " in " + guides.size() + " guides.");
            return 1;
        }
        long detectors = guides.stream().mapToLong(g -> g.detectors().size()).sum();
        System.out.println("OK: " + guides.size() + " guides, " + detectors + " detectors, " + examples + " examples. "
                + "Every Before example is caught, every detector is exercised, every After example is clean.");
        return 0;
    }

    // ------------------------------------------------------------------ Claude Code hook

    /**
     * PostToolUse hook. Reads the event JSON on stdin, checks only what the agent just wrote, and
     * reports on stderr with exit code 2 so the agent sees it. Any internal failure exits 0: a
     * broken linter must never get in the way of the work.
     */
    int hook() {
        try {
            String input = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
            Object event = new Json(input).parse();
            if (!(event instanceof Map<?, ?> root) || !(root.get("tool_input") instanceof Map<?, ?> toolInput)) return 0;
            if (!(toolInput.get("file_path") instanceof String filePath)) return 0;
            Path file = Path.of(filePath);
            if (!file.isAbsolute() && root.get("cwd") instanceof String cwd) file = Path.of(cwd).resolve(file);
            if (!Files.isRegularFile(file) || Files.size(file) > 2_000_000) return 0;

            List<Guide> guides = loadGuides(locateGuides());
            if (!interesting(unix(file), guides)) return 0;
            String level = System.getenv("MODERN_JAVA_HOOK_LEVEL");
            if (level != null && !level.isBlank()) minStatus = Status.parse(level);
            String text = Files.readString(file);

            List<String> written = new ArrayList<>();
            if (toolInput.get("new_string") instanceof String s) written.add(s);
            if (toolInput.get("edits") instanceof List<?> edits) {
                for (Object e : edits) if (e instanceof Map<?, ?> m && m.get("new_string") instanceof String s) written.add(s);
            }
            BitSet lines = null;
            if (!written.isEmpty()) {
                lines = linesContaining(text.replace("\r\n", "\n"), written);
            } else {
                Map<Path, BitSet> changed = gitChangedLines(file, "HEAD");
                Path key = file.toAbsolutePath().normalize();
                if (changed != null && changed.containsKey(key)) lines = changed.get(key);
            }
            Facts facts = factsFor(file);
            List<Finding> findings = scanText(file, text, facts, guides, lines);
            if (findings.isEmpty()) return 0;

            // One entry per guide, with every line it applies to, so a repeated pattern costs the agent one explanation.
            Map<Guide, List<Finding>> byGuide = new LinkedHashMap<>();
            for (Finding f : findings) byGuide.computeIfAbsent(f.guide(), k -> new ArrayList<>()).add(f);
            PrintStream err = System.err;
            err.println("modern-java-guidance: " + byGuide.size() + " legacy pattern" + (byGuide.size() == 1 ? "" : "s")
                    + " in what you just wrote to " + file.getFileName() + " (project: " + facts.describe() + ").");
            int shown = 0;
            List<Path> guideFiles = new ArrayList<>();
            for (Map.Entry<Guide, List<Finding>> e : byGuide.entrySet()) {
                if (shown++ == 10) {
                    err.println("- and " + (byGuide.size() - 10) + " more. Run the scanner on the file to see them all.");
                    break;
                }
                Guide g = e.getKey();
                List<Finding> hits = e.getValue();
                int more = hits.stream().mapToInt(Finding::more).sum();
                String where = hits.stream().map(f -> String.valueOf(f.line())).collect(Collectors.joining(", "));
                err.println("- line" + (hits.size() > 1 ? "s " : " ") + where + (more > 0 ? " (and " + more + " more)" : "")
                        + " [" + hits.get(0).status().label() + "] " + g.title() + ". " + g.note() + " (guide: " + g.id() + ")");
                guideFiles.add(g.file());
            }
            err.println("Fix these before moving on. Each guide has a full before and after:");
            for (Path guide : guideFiles) err.println("  " + guide);
            err.println("If a pattern is deliberate, keep it and add a comment on that line: // " + IGNORE + " <id> <reason>");
            return 2;
        } catch (Exception | StackOverflowError e) {
            if (System.getenv("MODERN_JAVA_DEBUG") != null) e.printStackTrace();
            return 0;
        }
    }

    /** Lines of {@code text} that fall inside any occurrence of the given fragments. Null if none is found. */
    static BitSet linesContaining(String text, List<String> fragments) {
        BitSet lines = new BitSet();
        boolean found = false;
        for (String fragment : fragments) {
            String needle = fragment.replace("\r\n", "\n");
            if (needle.isBlank()) {
                found = true;   // a deletion: nothing new to check
                continue;
            }
            for (int at = text.indexOf(needle); at >= 0; at = text.indexOf(needle, at + needle.length())) {
                found = true;
                lines.set(lineOf(text, at), lineOf(text, at + needle.length() - 1) + 1);
            }
        }
        return found ? lines : null;
    }

    /** A small recursive-descent JSON reader: objects become Map, arrays List, plus String, Double, Boolean, null. */
    static final class Json {
        private final String s;
        private int i;

        Json(String s) { this.s = s; }

        Object parse() {
            Object v = value();
            ws();
            if (i != s.length()) throw new IllegalArgumentException("trailing data at " + i);
            return v;
        }

        private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }

        private Object value() {
            ws();
            if (i >= s.length()) throw new IllegalArgumentException("unexpected end of JSON");
            char c = s.charAt(i);
            if (c == '{') {
                i++;
                Map<String, Object> map = new LinkedHashMap<>();
                ws();
                if (peek('}')) return map;
                do {
                    ws();
                    String key = string();
                    ws();
                    expect(':');
                    map.put(key, value());
                    ws();
                } while (peek(','));
                expect('}');
                return map;
            }
            if (c == '[') {
                i++;
                List<Object> list = new ArrayList<>();
                ws();
                if (peek(']')) return list;
                do {
                    list.add(value());
                    ws();
                } while (peek(','));
                expect(']');
                return list;
            }
            if (c == '"') return string();
            if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
            if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            int start = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            if (start == i) throw new IllegalArgumentException("unexpected character at " + i);
            return Double.parseDouble(s.substring(start, i));
        }

        private boolean peek(char c) {
            if (i < s.length() && s.charAt(i) == c) {
                i++;
                return true;
            }
            return false;
        }

        private void expect(char c) {
            if (!peek(c)) throw new IllegalArgumentException("expected '" + c + "' at " + i);
        }

        private String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') return sb.toString();
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                char e = s.charAt(i++);
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default: sb.append(e);
                }
            }
            throw new IllegalArgumentException("unterminated string");
        }
    }
}
