import me.austin.rush.bus.EventBus;
import me.austin.rush.bus.EventManager;
import me.austin.rush.listener.EventHandler;
import me.austin.rush.listener.LambdaListener;
import me.austin.rush.listener.Listener;

public class JavaTest {
    @EventHandler
    private final Listener<String> LISTENER = LambdaListener.listener(event -> System.out.println(event + " with higher priority!"), 1000);

    public static void main(String[] args) {
        final EventBus BUS = new EventManager();

        final Listener<String> LIST = LambdaListener.listener(event -> System.out.println(event + "!"));

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
