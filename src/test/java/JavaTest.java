import me.austin.rush.*;
import org.junit.jupiter.api.Test;

import static me.austin.rush.ListenerKt.listener;

final class JavaTest {
    @EventHandler
    private final LambdaListener LISTENER = listener(event -> System.out.println(event + " with higher priority!"), String.class, 1000);

    @Test
    public void test() {
        final EventBus BUS = new EventBus();
        final LambdaListener LIST = listener(event -> System.out.println(event + "!"), String.class);

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
