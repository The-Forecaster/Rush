import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.austin.light.EventBus
import me.austin.rush.*
import org.junit.jupiter.api.Test

class KotlinTest {
    @EventHandler
    val async = asyncListener<String> {
        delay(100)
        println("$it that's delayed for longer!")
    }

    @EventHandler
    val listener = listener<String>(priority = 60) { println("$it with higher priority!") }

    @Test
    fun test() {
        val async = asyncListener<String>(priority = 40) {
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

            unregisterAll(list, async)

            dispatch("I just posted another event")

            runBlocking {
                delay(200)
            }
        }
    }

    @Test
    fun test_lightweight() {
        val handler = EventBus.Handler<String>(-50) {
            println("$it!!")
        }

        with(EventBus(false)) {
            register<String> {
                println("$it!")
            }

            register(handler)

            post("I just posted an event")

            unregister(handler)

            post("I just posted another event")
        }
    }
}
