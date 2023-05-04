import kotlinx.coroutines.delay
import me.austin.rush.*
import org.junit.jupiter.api.Test

class KotlinTest {
    @Test
    fun test() {
        val listener = listener<String>({ println("$it!") })

        val async = asyncListener<String>({
            delay(10)
            println("$it that's delayed and with higher priority!")
        }, 200)

        with(EventManager()) {
            registerAll(listener, async)
            register(Main)
            dispatch("I just posted an event")
            dispatch(TestEvent())
        }
    }
}

object Main {
    @EventHandler
    val listener = listener<String>({ println("$it with higher priority!") }, 1000)

    @EventHandler
    val async = asyncListener<String>({
        delay(1000)
        println("$it that's delayed!")
    })
}

class TestEvent : Cancellable()