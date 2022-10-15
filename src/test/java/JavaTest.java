import me.austin.rush.EventManager;
import me.austin.rush.EventHandler;
import me.austin.rush.LambdaListener;
import me.austin.rush.Listener;

public class JavaTest {
    @EventHandler
    private final Listener<String> LISTENER = new LambdaListener<>(event -> System.out.println(event + " with higher priority!"), 1000);

    public static void main(String[] args) {
        final EventManager BUS = new EventManager();

        final Listener<String> LIST = new LambdaListener<>(event -> System.out.println(event + "!"));

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
