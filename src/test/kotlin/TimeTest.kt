import me.austin.rush.*
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.test.assertEquals

class TimeTest {
    // To test new EventBus models
    private fun bus_test(eventBus: EventBus) {
        var end = 0

        val list = Array<Listener>(10_000) { i ->
            listener<Int>(i % -150) { int ->
                end += i * (-1.0f).pow(int).toInt()
            }
        }

        val otherList = Array<Listener>(10_000) { i ->
            listener<Int>(i % -86) { int ->
                end += int * (-1.0f).pow(i).toInt()
            }
        }

        val start = System.currentTimeMillis()

        with(eventBus) {
            registerAll(*list)
            registerAll(*otherList)

            for (i in 0..10_000) {
                dispatch(i)
            }

            unregisterAll(*list)

            for (i in 0..10_000) {
                dispatch(i / 2)
            }
        }

        println("End: $end")
        println("Test took ${System.currentTimeMillis() - start}ms.")
    }

    @Test
    fun regular_test() {
        bus_test(EventDispatcher())
    }

    @Test
    fun concurrent_test() {
        bus_test(ConcurrentEventDispatcher())
    }
}
