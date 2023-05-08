import me.austin.rush.EventBus;
import me.austin.rush.EventHandler;
import me.austin.rush.LambdaListener;
import org.junit.jupiter.api.Test;

final class JavaTest {
    @EventHandler
    private final LambdaListener<String> LISTENER = new LambdaListener<>(event -> System.out.println(event + " with higher priority!"), 1000, String.class);

    @Test
    public void test() {
        final EventBus BUS = new EventBus();

        final LambdaListener<String> LIST = new LambdaListener<>(event -> System.out.println(event + "!"), String.class);

        BUS.register(LIST);
        BUS.register(new JavaTest());

        BUS.dispatch("I just posted an event");
    }
}
