import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.austin.light.EventManager
import me.austin.rush.EventBus
import me.austin.rush.EventHandler
import me.austin.rush.asyncListener
import me.austin.rush.listener
import org.junit.jupiter.api.Test

class KotlinTest {
    @EventHandler
    val async = asyncListener<String> {
        delay(1000)
        println("$it that's delayed for longer!!")
    }

    @EventHandler
    val listener = listener<String>({ println("$it with higher priority!") }, 1000)

  //  @Test
    fun test() {
        val async = asyncListener<String>({
            delay(10)
            println("$it that's delayed!")
        }, 200)

        val listener = listener<String> { println("$it!") }

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
        val act: (String) -> Unit = {
            println("$it!!!!!!!")
        }

        with(EventManager(false)) {
            register<String> {
                println("$it!")
            }

            register<String> {
                println("$it!!")
            }

            register<String> {
                println("$it!!!")
            }

            register(act)

            post("I just posted an event")

            unregister(act)

            post("I just posted another event")
        }
    }
}