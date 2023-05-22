import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.austin.light.EventManager
import me.austin.light.handler
import me.austin.rush.EventBus
import me.austin.rush.EventHandler
import me.austin.rush.asyncListener
import me.austin.rush.listener
import org.junit.jupiter.api.Test

class KotlinTest {
    @EventHandler
    val listener = listener<String>({ println("$it with higher priority!") }, 1000)

    @EventHandler
    val async = asyncListener<String> {
        delay(1000)
        println("$it that's delayed for longer!!")
    }

    @Test
    fun test() {
        val listener = listener<String> { println("$it!") }

        val async = asyncListener<String>({
            delay(10)
            println("$it that's delayed!")
        }, 200)

        with(EventBus()) {
            registerAll(listener, async)
            register(KotlinTest())
            dispatch("I just posted an event")
        }

        runBlocking {
            delay(1100)
        }
    }

    @Test
    fun test_lightweight() {
        val manager = EventManager()

        manager.register<String> {
            println("$it!")
        }

        manager.register<String>(handler<String> {
            println("$it again!")
        })

        manager.post("I just posted an event")
    }
}