import me.austin.rush.ConcurrentEventDispatcher;
import me.austin.rush.EventHandler;
import me.austin.rush.LambdaListener;
import org.junit.jupiter.api.Test;

import static me.austin.rush.ListenerImplKt.listener;

final class JavaTest {
    @EventHandler
    private final LambdaListener LISTENER = listener(String.class, 100, event -> System.out.println(event + " with higher priority!"));

    @Test
    public void test() {
        final ConcurrentEventDispatcher BUS = new ConcurrentEventDispatcher();
        final LambdaListener LIST = listener(String.class, event -> System.out.println(event + "!"));

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
