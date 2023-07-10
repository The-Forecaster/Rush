import me.austin.rush.*
import org.junit.jupiter.api.Test
import kotlin.math.pow

class TimeTest {
    // To test new EventBus models
    private fun bus_test(eventBus: ReflectionBus) {
        var end = 0

        val list = Array<Listener>(5_000) { i ->
            listener<Int>(i % -35) { int ->
                end += i * (-1.0f).pow(int).toInt()
            }
        }

        val otherList = Array<Listener>(5_000) { i ->
            listener<Int>(i % -86) { int ->
                end += int * (-1.0f).pow(i).toInt()
            }
        }

        val start = System.currentTimeMillis()

        with(eventBus) {
            subscribeAll(*list)
            subscribeAll(*otherList)

            for (i in 0..1_000) {
                post(i)
            }

            unsubscribeAll(*list)

            for (i in 0..1_000) {
                post(i / 2)
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
