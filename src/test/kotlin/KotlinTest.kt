import me.austin.rush.EventHandler
import me.austin.rush.EventManager
import me.austin.rush.listener

fun main() {
    val listener = listener<String>({ println("$it!") })

    with(EventManager()) {
        register(listener)
        register(Main)
        dispatch("I just posted an event")
    }
}

object Main {
    @EventHandler
    val listener = listener<String>({ println("$it with higher priority!") }, 1000)
}