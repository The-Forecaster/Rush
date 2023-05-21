import me.austin.rush.EventBus;
import me.austin.rush.EventHandler;
import me.austin.rush.LambdaListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

final class JavaTest {
    @EventHandler
    private final LambdaListener<String> LISTENER = new LambdaListener<>(event -> System.out.println(event + " with higher priority!"), 1000, String.class);

    @Test
    public void test() {
        final EventBus BUS = new EventBus();

        final LambdaListener<String> LIST = new LambdaListener<>(event -> System.out.println(event + "!"), String.class);

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }

    @Test
    public void time_test() {
        int fin = 0;
        final AtomicInteger index = new AtomicInteger();
        final var arr = Arrays.stream(new int[1_000_000]).map(num -> index.addAndGet(1)).toArray();
        final long time = System.currentTimeMillis();

        for (int i : arr) {
            fin += i;
        }

        System.out.println("fin is " + (fin) + " after " + (System.currentTimeMillis() - time) + "ms");
    }

    @Test
    public void time_other() {
        int fin = 0;
        final AtomicInteger index = new AtomicInteger();
        final var list = Arrays.stream(new Integer[1_000_000]).map(num -> index.addAndGet(1)).toList();
        final long time = System.currentTimeMillis();

        for (final int num : list) {
            fin += num;
        }

        System.out.println("fin is " + (fin) + " after " + (System.currentTimeMillis() - time) + "ms");
    }
}
