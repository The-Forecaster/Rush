import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.austin.rush.FastEventBus
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

        with(FastEventBus()) {
            subscribeAll(list, async)

            subscribe(KotlinTest())

            post("I just posted an event")

            runBlocking {
                delay(200)
            }

            unsubscribeAll(listOf(list, async))

            post("I just posted another event")

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

        with(FastEventBus()) {
            subscribeAll(list, async)

            subscribe(KotlinTest())

            post("I just posted an event")

            runBlocking {
                delay(200)
            }

            unsubscribeAll(listOf(list, async))

            post("I just posted another event")

            runBlocking {
                delay(200)
            }
        }
    }
}