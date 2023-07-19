import me.austin.rush.*
import org.junit.jupiter.api.Test
import kotlin.math.pow
import kotlin.test.assertEquals

var end = 0

class Container(private val i: Int) {
    @EventHandler
    val listener = listener<Int>(i % 42) { int ->
        end += (-1.0f).pow(i).toInt() * i.toFloat().pow(int).toInt() * int.toFloat().pow(i).toInt()
    }
}

class TimeTest {
    // To test new EventBus models
    private fun bus_test(eventBus: EventBus) {
        end = 0

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

        eventBus.subscribeAll(*list)
        eventBus.subscribeAll(*otherList)

        println("Subscription took ${System.currentTimeMillis() - start}ms")
        var dem = System.currentTimeMillis()

        for (i in 0..1_000) {
            eventBus.post(i)
        }

        println("First post took ${System.currentTimeMillis() - dem}ms")
        dem = System.currentTimeMillis()

        eventBus.unsubscribeAll(*list)

        println("Unsubscribe took ${System.currentTimeMillis() - dem}ms")
        dem = System.currentTimeMillis()

        for (i in 0..1_000) {
            eventBus.post(i / 2)
        }

        println("Second post took ${System.currentTimeMillis() - dem}ms")
        println("Test took ${System.currentTimeMillis() - start}ms")

        // assertEquals(124750, end)
        println("End: $end")
    }

    private fun reflection_bus_test(eventBus: ReflectionEventBus) {
        end = 0

        val list = Array<Listener>(5_000) { i ->
            listener<Int>(i % -35) { int ->
                end += i * (-1.0f).pow(int).toInt()
            }
        }

        val otherList = Array<Listener>(5_000) { i ->
            listener<Int>((i % -8.6).toInt()) { int ->
                end += int * (-1.0f).pow(i).toInt()
            }
        }

        val objList = Array(500) { i ->
            Container(i)
        }

        val start = System.currentTimeMillis()

        eventBus.subscribeAll(*list)
        eventBus.subscribeAll(*otherList)
        eventBus.subscribeAll(*objList)

        println("Subscription took ${System.currentTimeMillis() - start}ms")
        var dem = System.currentTimeMillis()

        for (i in 0..1_000) {
            eventBus.post(i)
        }

        println("First post took ${System.currentTimeMillis() - dem}ms")
        dem = System.currentTimeMillis()

        eventBus.unsubscribeAll(*list)
        eventBus.unsubscribeAll(*objList)

        println("Unsubscribe took ${System.currentTimeMillis() - dem}ms")
        dem = System.currentTimeMillis()

        for (i in 0..1_000) {
            eventBus.post(i / 2)
        }

        println("Second post took ${System.currentTimeMillis() - dem}ms")
        println("Test took ${System.currentTimeMillis() - start}ms")

        // assertEquals(124750, end)
        println("End: $end")
    }

    @Test
    fun test() {
        println("--LightEventBus--")
        bus_test(LightEventBus())
        println("\n--FastEventBus--")
        bus_test(FastEventBus())
        println("\n--ConcurrentEventBus--")
        bus_test(ConcurrentEventBus())

        print("\rReflection")
        println("\n--FastEventBus--")
        reflection_bus_test(FastEventBus())
        println("\n--ConcurrentEventBus--")
        reflection_bus_test(ConcurrentEventBus())
    }
}
