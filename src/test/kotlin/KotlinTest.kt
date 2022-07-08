import me.austin.rush.bus.EventManager
import me.austin.rush.listener.EventHandler
import me.austin.rush.listener.listener

val bus = EventManager()

fun main() {
    val listener = listener<String> { println(it) }

    with(bus) {
        register(listener)
        register(Main)
        dispatch("I just posted an event!")
    }
}

object Main {
    @EventHandler
    val listener = listener<String> { println("$it again!")}
}