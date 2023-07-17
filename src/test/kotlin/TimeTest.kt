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

        // assertEquals(12497500, end)
        println("End: $end")
        return System.currentTimeMillis() - start
    }

    @Test
    fun test() {
        println("LightEventBus took ${bus_test(LightEventBus())}ms")
        println("FastEventBus took ${bus_test(FastEventBus())}ms")
        println("ConcurrentEventBus took ${bus_test(ConcurrentEventBus())}ms")
    }
}
