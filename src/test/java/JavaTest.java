import me.austin.rush.EventBus;
import me.austin.rush.EventHandler;
import me.austin.rush.LambdaListener;
import org.junit.jupiter.api.Test;

import static me.austin.rush.ListenerKt.listener;

final class JavaTest {
    @EventHandler
    private final LambdaListener LISTENER = listener(String.class, 1000, event -> System.out.println(event + " with higher priority!"));

    @Test
    public void test() {
        final EventBus BUS = new EventBus();
        final LambdaListener LIST = listener(String.class, event -> System.out.println(event + "!"));

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
