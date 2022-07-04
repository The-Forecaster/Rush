import me.austin.rush.bus.impl.EventManager
import me.austin.rush.listener.impl.listener

fun main() {
    val bus = EventManager()
    val listener = listener<String> { println(it) }

    bus.register(listener)
    bus.dispatch("I just posted an event!")
}
