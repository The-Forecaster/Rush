import me.austin.rush.bus.EventBus;
import me.austin.rush.bus.EventManager;
import me.austin.rush.listener.EventHandler;
import me.austin.rush.listener.Listener;

import static me.austin.rush.listener.ListenerImplKt.HIGHEST;
import static me.austin.rush.listener.ListenerImplKt.listener;

public class JavaTest {
    @EventHandler
    private final Listener<String> LISTENER = listener(event -> System.out.println(event + " again!"), HIGHEST);

    public static void main(String[] args) {
        final EventBus BUS = new EventManager();

        final Listener<String> LIST = listener(event -> System.out.println(event + "!"));

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
