package me.austin.light

import me.austin.rush.EventBus
import me.austin.rush.Listener
import java.util.*
import kotlin.reflect.KClass

/**
 * Extremely lightweight event-bus made for kotlin use.
 * This does not contain any reflection, so you will either have to directly register lambda functions or instantiate a [Listener] object and then register that.
 *
 * @author Austin
 * @since 2023
 *
 * @param recursive If the bus will also post superclasses of events posted.
 */
class FastEventBus(private val recursive: Boolean = true) : EventBus {
    /**
     * For all the classes of events and the lambdas which target them.
     */
    private val subscribers = mutableMapOf<KClass<*>, Array<Listener>>()

    /**
     * This is so we only ever have 1 write action going on at a time.
     */
    // Need to make sure this won't double lock, more tests coming in the future
    private val writeSync = Any()

    /**
     * Adds a [Listener] to the [subscribers].
     *
     * @param listener The handler to be added to the [subscribers].
     */
    override fun subscribe(listener: Listener) {
        synchronized(this.writeSync) {
            val array = this.subscribers[listener.target]

            if (array == null) {
                this.subscribers[listener.target] = arrayOf(listener)
            } else {
                if (listener in array) {
                    return
                }

                val newArray = arrayOfNulls<Listener>(array.size + 1)

                val index = Arrays.binarySearch(array, listener).let { i ->
                    if (i < 0) {
                        -i - 1
                    } else {
                        i
                    }
                }

                System.arraycopy(array, 0, newArray, 0, index)
                newArray[index] = listener
                System.arraycopy(array, index, newArray, index + 1, array.size - index)

                @Suppress("UNCHECKED_CAST")
                this.subscribers[listener.target] = newArray as Array<Listener>
            }
        }
    }

    /**
     * Adds a lambda to the registry, with a certain priority if desired.
     * Note that this lambda cannot be removed.
     *
     * @param priority The priority which you want this lambda to be added at. Will default to `-50`.
     * @param action Lambda to add to the [subscribers].
     */
    inline fun <reified T : Any> subscribe(priority: Int = -50, noinline action: (T) -> Unit) {
        this.subscribe(object : Listener {
            @Suppress("UNCHECKED_CAST")
            private val action = action as (Any) -> Unit

            override val target: KClass<*>
                get() = T::class
            override val priority: Int
                get() = priority

            override fun invoke(param: Any) {
                action(param)
            }
        })
    }

    /**
     * Removes a [Listener] from the [subscribers].
     *
     * @param listener The handler to remove from the [subscribers].
     */
    override fun unsubscribe(listener: Listener) {
        synchronized(writeSync) {
            val array = this.subscribers[listener.target]

            if (array != null) {
                if (array.size > 1) {
                    val index = array.indexOf(listener)

                    if (index < 0) {
                        return
                    }

                    val newArray = arrayOfNulls<Listener>(array.size - 1)

                    System.arraycopy(array, 0, newArray, 0, index)
                    System.arraycopy(array, index + 1, newArray, index, array.size - index - 1)

                    @Suppress("UNCHECKED_CAST")
                    this.subscribers[listener.target] = newArray as Array<Listener>
                } else {
                    this.subscribers.remove(listener.target)
                }
            }
        }
    }

    /**
     * For dispatching events.
     *
     * @see <a href="https://github.com/x4e/EventDispatcher/">cookiedragon event bus</a> for my inspiration.
     *
     * @param event Event to be posted to all registered actions.
     */
    fun post(event: Any) {
        this.subscribers[event::class]?.let { array ->
            for (handler in array) {
                handler(event)
            }
        }

        if (this.recursive) {
            var clazz: Class<*>? = event.javaClass.superclass

            while (clazz != null) {
                this.subscribers[clazz.kotlin]?.let { array ->
                    for (handler in array) {
                        handler(event)
                    }
                }
                clazz = clazz.superclass
            }
        }
    }
}