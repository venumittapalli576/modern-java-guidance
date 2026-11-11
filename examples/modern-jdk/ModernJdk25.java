import java.util.List;

/**
 * "After" code that needs a newer JDK than 21: unnamed variables (Java 22) and scoped values
 * (Java 25). CI compiles and runs it on JDK 25.
 *
 *     javac --release 25 -Xlint:deprecation,removal -Werror -d out examples/modern-jdk/ModernJdk25.java
 */
public class ModernJdk25 {

    record RequestContext(String tenant) {}

    // concurrency/scoped-values
    private static final ScopedValue<RequestContext> CONTEXT = ScopedValue.newInstance();

    static String currentTenant() {
        return CONTEXT.get().tenant();
    }

    // language/unnamed-variables
    static int parse(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException _) {
            return fallback;
        }
    }

    public static void main(String[] args) {
        ScopedValue.where(CONTEXT, new RequestContext("acme")).run(() -> System.out.println(currentTenant()));
        System.out.println(parse("x", 7) + " " + List.of(1, 2, 3).getLast());
    }
}
