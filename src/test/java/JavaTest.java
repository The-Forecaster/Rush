import me.austin.rush.ConcurrentEventBus;
import me.austin.rush.EventHandler;
import me.austin.rush.LambdaListener;
import org.junit.jupiter.api.Test;

import static me.austin.rush.ListenerImplKt.listener;

final class JavaTest {
    @EventHandler
    private final LambdaListener LISTENER = listener(String.class, 100, event -> System.out.println(event + " with higher priority!"));

    @Test
    public void test() {
        final ConcurrentEventBus BUS = new ConcurrentEventBus();
        final LambdaListener LIST = listener(String.class, event -> System.out.println(event + "!"));

        BUS.subscribe(LIST);
        BUS.subscribe(new JavaTest());

        BUS.post("I just posted an event");
    }
}
