import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The "After" side of the language, jdk-apis and concurrency guides, as one file that compiles
 * with nothing but a JDK 21. CI compiles it on every push, so the guides cannot drift into
 * code that does not build.
 *
 *     javac --release 21 -Xlint:deprecation,removal -Werror -d out examples/modern-jdk/ModernJdk.java
 */
public class ModernJdk {

    // language/records
    record Money(long cents, String currency) {
        Money {
            Objects.requireNonNull(currency);
        }
    }

    // language/pattern-matching-switch
    sealed interface Shape permits Circle, Rectangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}

    static double area(Shape shape) {
        return switch (shape) {
            case Circle(double radius) -> Math.PI * radius * radius;
            case Rectangle(double width, double height) -> width * height;
        };
    }

    // language/pattern-matching-instanceof
    static double radiusOf(Object shape) {
        if (shape instanceof Circle circle) {
            return circle.radius();
        }
        return 0;
    }

    // language/switch-expressions
    enum Status { PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED }

    static String label(Status status) {
        return switch (status) {
            case PENDING, PROCESSING -> "In progress";
            case SHIPPED -> "On its way";
            case DELIVERED, CANCELLED -> "Closed";
        };
    }

    // language/text-blocks
    static final String QUERY = """
            SELECT o.id, o.total
            FROM orders o
            WHERE o.status = 'OPEN'
            ORDER BY o.created_at DESC
            """;

    // language/string-templates-withdrawn
    static String describe(long id, Money total) {
        return "Order %s total: %s".formatted(id, total);
    }

    record Customer(String lastName, String email) {}

    // language/lambdas, stream-to-list, optional-idioms, collection-factory-methods, sequenced-collections
    static final List<String> ROLES = List.of("ADMIN", "USER", "AUDITOR");
    static final Set<String> REGIONS = Set.of("us", "eu");

    static List<String> emails(List<Customer> customers) {
        List<Customer> sorted = new ArrayList<>(customers);
        sorted.sort(Comparator.comparing(Customer::lastName));
        return sorted.stream().map(Customer::email).toList();
    }

    static String nameOf(Optional<Customer> customer) {
        return customer.map(Customer::lastName).orElse("guest");
    }

    static String latest(List<String> events) {
        return events.getLast();
    }

    // language/boxed-primitive-constructors, string-is-blank, legacy-collections
    static void basics(String rawId, String name) {
        Integer count = 42;
        Long id = Long.valueOf(rawId);
        boolean active = Boolean.parseBoolean("true");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        List<String> names = new ArrayList<>();
        Map<String, Integer> counts = new HashMap<>();
        Deque<String> frames = new ArrayDeque<>();
        StringBuilder sb = new StringBuilder();
        sb.append(count).append(id).append(active).append(names).append(counts).append(frames);
    }

    // language/finalizers
    static class NativeBuffer implements AutoCloseable {
        @Override
        public void close() {
            // release the resource deterministically
        }
    }

    // language/files-read-string
    static void files(String report) throws Exception {
        String template = Files.readString(Path.of("template.html"));
        Files.writeString(Path.of("report.txt"), report + template);
    }

    // jdk-apis/java-time
    static String dueDate(Clock clock) {
        LocalDate dueDate = LocalDate.now(clock).plusDays(30);
        long days = ChronoUnit.DAYS.between(LocalDate.of(2026, 1, 15), LocalDate.now(clock));
        return dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + days;
    }

    // jdk-apis/jdk-http-client, url-constructors
    static String fetch() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.example.com/rates"))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        URL endpoint = URI.create("https://api.example.com/v1/orders").toURL();
        return response.body() + endpoint;
    }

    // jdk-apis/locale-of, thread-id, base64-and-hex, standard-charsets
    static String misc(byte[] bytes, String term) throws Exception {
        Locale locale = Locale.of("en", "IN");
        long thread = Thread.currentThread().threadId();
        String token = Base64.getEncoder().encodeToString(bytes);
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        byte[] decoded = Base64.getDecoder().decode(token);
        byte[] body = term.getBytes(StandardCharsets.UTF_8);
        String query = URLEncoder.encode(term, StandardCharsets.UTF_8);
        return locale + " " + thread + digest + decoded.length + body.length + query;
    }

    // jdk-apis/unsafe-memory-access
    static class Counter {
        private static final VarHandle COUNT;

        static {
            try {
                COUNT = MethodHandles.lookup().findVarHandle(Counter.class, "count", int.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @SuppressWarnings("unused")
        private volatile int count;

        boolean increment(int expected) {
            return COUNT.compareAndSet(this, expected, expected + 1);
        }
    }

    // jdk-apis/class-new-instance, process-builder
    static Object reflect(String className, int count) throws Exception {
        Object plugin = Class.forName(className).getDeclaredConstructor().newInstance();
        Process process = new ProcessBuilder("git", "log", "--oneline", "-n", String.valueOf(count))
                .redirectErrorStream(true)
                .start();
        return List.of(plugin, process);
    }

    // concurrency/virtual-threads, structured-concurrency-preview (the final-API alternative)
    static String dashboard() throws ExecutionException, InterruptedException {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> user = executor.submit(() -> "user");
            Future<List<String>> orders = executor.submit(() -> List.of("order"));
            return user.get() + orders.get();
        }
    }

    // concurrency/synchronized-no-longer-pins
    private String cache = "";

    synchronized void refresh() {
        cache = "loaded";
    }

    // concurrency/scheduled-executor
    static ScheduledExecutorService schedule(Runnable evictExpired) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(evictExpired, 0, 1, TimeUnit.MINUTES);
        return scheduler;
    }

    public static void main(String[] args) {
        System.out.println(area(new Circle(1)) + " " + label(Status.SHIPPED) + " " + describe(7, new Money(1250, "USD")));
        System.out.println(emails(List.of(new Customer("Rao", "rao@example.com"))) + " " + latest(ROLES) + " " + REGIONS.size());
        System.out.println(dueDate(Clock.systemUTC()) + " " + nameOf(Optional.empty()) + " " + radiusOf(new Circle(2)));
        System.out.println(new Counter().increment(0) + " " + QUERY.lines().count());
    }
}
