import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.austin.light.EventBus
import me.austin.rush.*
import org.junit.jupiter.api.Test

class KotlinTest {
    @EventHandler
    val async = asyncListener<String> {
        delay(100)
        println("$it that's delayed for longer!!")
    }

    @EventHandler
    val listener = listener<String>({ println("$it with higher priority!") }, 60)

    @Test
    fun test() {
        val async = asyncListener<String>({
            delay(10)
            println("$it that's delayed!")
        }, 40)

        val listener = listener<String> { println("$it!") }

        with(EventDispatcher()) {
            registerAll(listener, async)

            dispatch("I just posted an event")

            runBlocking {
                delay(200)
            }

            unregisterAll(listener, async)

            dispatch("I just posted another event")

            runBlocking {
                delay(200)
            }
        }
    }

    @Test
    fun test_lightweight() {
        val act: (String) -> Unit = {
            println("$it!!!!!!!")
        }

        with(EventBus(false)) {
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

            val event = CancellableEvent()

            post(event) {
                for (action in it) {
                    action(event)

                    if (event.isCancelled) {
                        break
                    }
                }
            }
        }
    }
}

class CancellableEvent : Cancellable()