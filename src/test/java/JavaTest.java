import me.austin.rush.bus.EventBus;
import me.austin.rush.bus.EventManager;
import me.austin.rush.listener.Listener;

import static me.austin.rush.listener.ListenerImplKt.listener;

public class JavaTest {
    public static void main(String[] args) {
        EventBus BUS = new EventManager();
        Listener<String> LISTENER = listener(System.out::println);

        BUS.register(LISTENER);

        BUS.dispatch("I just posted an event!");
    }
}
