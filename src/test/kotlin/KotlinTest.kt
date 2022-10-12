import me.austin.rush.bus.EventManager
import me.austin.rush.listener.EventHandler
import me.austin.rush.listener.listener

fun main() {
    val listener = listener<String> { println("$it!") }

    with(EventManager()) {
        register(listener)
        register(Main)
        dispatch("I just posted an event")
    }
}

object Main {
    @EventHandler
    val listener = listener<String>(action = { println("$it with higher priority!")}, priority = 1000)
}