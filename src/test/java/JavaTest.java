import me.austin.rush.bus.EventBus;
import me.austin.rush.bus.EventManager;
import me.austin.rush.listener.Listener;

import static me.austin.rush.listener.ListenerImplKt.listener;

public class JavaTest {
    private final Listener<String> LISTENER = listener(event -> System.out.println("${event} again!"));

    public static void main(String[] args) {
        EventBus BUS = new EventManager();

        Listener<String> LIST = listener(System.out::println);

        BUS.register(new JavaTest());
        BUS.register(LIST);

        BUS.dispatch("I just posted an event!");
    }
}
