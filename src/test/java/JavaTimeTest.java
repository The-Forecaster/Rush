import me.austin.rush.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static me.austin.rush.ListenerImplKt.listener;


class JavaContainer {
    private final AtomicInteger end;

    @EventHandler
    public final Listener list;

    public JavaContainer(final int i, final AtomicInteger end) {
        this.end = end;

        this.list = listener(Integer.class, i % 42, x -> this.end.addAndGet((int) (Math.pow((-1), i) * x)));
    }
}

public class JavaTimeTest {
    private void bus_test(final EventBus eventBus) {
        final var end = new AtomicInteger();

        final var listeners = new Listener[5_000];

        for (var i = 0; i < listeners.length; i++) {
            final var finalI = i;
            listeners[i] = listener(Integer.class, i % 35, x -> end.addAndGet((int) (finalI * Math.pow(-1.0, x))));
        }

        final var otherListeners = new Listener[5_000];

        for (var i = 0; i < listeners.length; i++) {
            final var finalI = i;
            otherListeners[i] = listener(Integer.class, x -> end.addAndGet((int) (x * (Math.pow(-1.0, finalI)))));
        }

        final var time = System.currentTimeMillis();

        eventBus.subscribeAll(listeners);
        eventBus.subscribeAll(otherListeners);

        System.out.println("Subscription took " + (System.currentTimeMillis() - time) + "ms");
        var inc = System.currentTimeMillis();

        for (var i = 0; i < 1_000; i++) {
            eventBus.post(i);
        }

        System.out.println("First post took " + (System.currentTimeMillis() - inc) + "ms");
        inc = System.currentTimeMillis();

        eventBus.unsubscribeAll(otherListeners);

        System.out.println("Unsubscribe took " + (System.currentTimeMillis() - inc) + "ms");
        inc = System.currentTimeMillis();

        for (var i = 0; i < 1_000; i++) {
            eventBus.post(i / 2);
        }

        System.out.println("Second post took " + (System.currentTimeMillis() - inc) + "ms");
        System.out.println("Test took " + (System.currentTimeMillis() - time) + "ms");

        System.out.println("End: " + end.get());
    }

    private void reflection_bus_test(final ReflectionEventBus eventBus) {
        final var end = new AtomicInteger();

        final var listeners = new Listener[5_000];

        for (var i = 0; i < listeners.length; i++) {
            final var finalI = i;
            listeners[i] = listener(Integer.class, i % 35, x -> end.addAndGet((int) (finalI * Math.pow(-1.0, x))));
        }

        final var otherListeners = new Listener[5_000];

        for (var i = 0; i < otherListeners.length; i++) {
            final var finalI = i;
            otherListeners[i] = listener(Integer.class, x -> end.addAndGet((int) (x * (Math.pow(-1.0, finalI)))));
        }

        final var containerList = new JavaContainer[2_000];

        for (var i = 0; i < containerList.length; i++) {
            containerList[i] = new JavaContainer(i, end);
        }

        final var time = System.currentTimeMillis();

        eventBus.subscribeAll(listeners);
        eventBus.subscribeAll(otherListeners);
        eventBus.subscribeAll((Object[]) containerList);

        System.out.println("Subscription took " + (System.currentTimeMillis() - time) + "ms");
        var inc = System.currentTimeMillis();

        for (var i = 0; i < 1_000; i++) {
            eventBus.post(i);
        }

        System.out.println("First post took " + (System.currentTimeMillis() - inc) + "ms");
        inc = System.currentTimeMillis();

        eventBus.unsubscribeAll(otherListeners);
        eventBus.unsubscribeAll((Object[]) containerList);

        System.out.println("Unsubscribe took " + (System.currentTimeMillis() - inc) + "ms");
        inc = System.currentTimeMillis();

        for (var i = 0; i < 1_000; i++) {
            eventBus.post(i / 2);
        }

        System.out.println("Second post took " + (System.currentTimeMillis() - inc) + "ms");
        System.out.println("Test took " + (System.currentTimeMillis() - time) + "ms");

        System.out.println("End: " + end.get());
    }

    @Test
    public void test() {
        System.out.println("--LightEventBus--");
        bus_test(new LightEventBus());

        System.out.println("\n--FastEventBus--");
        bus_test(new FastEventBus());
        System.out.println("\n--ConcurrentEventBus--");
        bus_test(new ConcurrentEventBus());

        System.out.println("\rReflection");
        System.out.println("\n--FastEventBus--");
        reflection_bus_test(new FastEventBus());
        System.out.println("\n--ConcurrentEventBus--");
        reflection_bus_test(new ConcurrentEventBus());
    }
}

