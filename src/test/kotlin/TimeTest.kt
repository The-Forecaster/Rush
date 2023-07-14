import me.austin.rush.*
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.test.assertEquals

class TimeTest {
    // To test new EventBus models
    private fun bus_test(eventBus: EventBus): Long {
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

        assertEquals(end, 12497500)
        println("End: $end")
        return System.currentTimeMillis() - start
    }

    @Test
    fun test() {
        println("EventDispatcher took ${bus_test(EventDispatcher())}ms")
        println("ConcurrentEventDispatcher took ${bus_test(ConcurrentEventDispatcher())}ms")
    }
}
