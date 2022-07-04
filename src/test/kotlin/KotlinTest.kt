import me.austin.rush.bus.EventManager
import me.austin.rush.listener.listener

fun main() {
    val bus = EventManager()
    val listener = listener<String> { println(it) }

    bus.register(listener)
    bus.dispatch("I just posted an event!")
}
