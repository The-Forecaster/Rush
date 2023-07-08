import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.austin.light.EventBus
import me.austin.rush.EventDispatcher
import me.austin.rush.EventHandler
import me.austin.rush.asyncListener
import me.austin.rush.listener
import org.junit.jupiter.api.Test

class KotlinTest {
    @EventHandler
    val async = asyncListener<String> {
        delay(100)
        println("$it that's delayed for longer!")
    }

    @EventHandler
    val listener = listener<String>(60) { println("$it with higher priority!") }

    @EventHandler
    val l = listener<String> {
        println("Stuff: $it")
    }

    @EventHandler
    val ll = listener<String> {
        println("Stuff more: $it")
    }

    @Test
    fun test() {
        val async = asyncListener<String>(40) {
            delay(10)
            println("$it that's delayed!")
        }

        val list = listener<String> { println("$it!") }

        with(EventDispatcher()) {
            registerAll(list, async)

            register(KotlinTest())

            dispatch("I just posted an event")

            runBlocking {
                delay(200)
            }

            unregisterAll(listOf(list, async))

            dispatch("I just posted another event")

            runBlocking {
                delay(200)
            }
        }
    }

    @Test
    fun test_concurrent() {
        val async = asyncListener<String>(40) {
            delay(10)
            println("$it that's delayed!")
        }

        val list = listener<String> { println("$it!") }

        with(EventDispatcher()) {
            registerAll(list, async)

            register(KotlinTest())

            dispatch("I just posted an event")

            runBlocking {
                delay(200)
            }

            unregisterAll(listOf(list, async))

            dispatch("I just posted another event")

            runBlocking {
                delay(200)
            }
        }
    }

    @Test
    fun test_lightweight() {
        val handler = EventBus.Handler<String>(0) {
            println("$it with higher priority")
        }

        with(EventBus(false)) {
            register<String>(1) {
                println("$it!")
            }

            register(handler)

            post("I just posted an event")

            unregister(handler)

            post("I just posted another event")
        }
    }
}