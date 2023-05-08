import kotlinx.coroutines.delay
import me.austin.rush.EventHandler
import me.austin.rush.EventBus
import me.austin.rush.asyncListener
import me.austin.rush.listener
import org.junit.jupiter.api.Test

class KotlinTest {
    @EventHandler
    val listener = listener<String>({ println("$it with higher priority!") }, 1000)

    @EventHandler
    val async = asyncListener<String>({
        delay(1000)
        println("$it that's delayed!")
    })

    @Test
    fun test() {
        val listener = listener<String>({ println("$it!") })

        val async = asyncListener<String>({
            delay(10)
            println("$it that's delayed and with higher priority!")
        }, 200)

        with(EventBus()) {
            registerAll(listener, async)
            register(KotlinTest())
            dispatch("I just posted an event")
        }
    }
}