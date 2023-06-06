import me.austin.rush.EventDispatcher;
import me.austin.rush.EventHandler;
import me.austin.rush.LambdaListener;
import org.junit.jupiter.api.Test;

import static me.austin.rush.ListenerImplKt.listener;

final class JavaTest {
    @EventHandler
    private final LambdaListener LISTENER = listener(event -> System.out.println(event + " with higher priority!"), String.class, (byte) 8);

    @Test
    public void test() {
        final EventDispatcher BUS = new EventDispatcher();
        final LambdaListener LIST = listener(event -> System.out.println(event + "!"), String.class);

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
