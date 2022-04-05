import me.austin.rush.bus.impl.EventManager

fun main(args: Array<String>) {
    val bus = EventManager()
}

data class Foo(val bar: Int)