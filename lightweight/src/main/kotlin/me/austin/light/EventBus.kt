package me.austin.light

import me.austin.light.EventBus.Handler
import java.util.*
import kotlin.reflect.KClass

/**
 * Extremely lightweight event-bus made for kotlin use.
 * This does not contain any reflection, so you will either have to directly register lambda functions or instantiate a [Handler] object and then register that.
 *
 * @author Austin
 * @since 2023
 *
 * @param recursive If the bus will also post superclasses of events posted.
 */
class EventBus(private val recursive: Boolean = true) {
    /**
     * For all the classes of events and the lambdas which target them.
     */
    private val subscribers = mutableMapOf<KClass<*>, Array<Handler<*>>>()

    /**
     * This is so we only ever have 1 write action going on at a time.
     */
    // Need to make sure this won't double lock, more tests coming in the future
    private val writeSync = Any()

    /**
     * Adds a [Handler] to the registry with the [KClass] target explicitly stated.
     *
     * @param type Type that the [Handler] will accept.
     * @param handler The [Handler] to add to the [subscribers]
     */
    fun <T : Any> subscribe(type: KClass<T>, handler: Handler<T>) {
        synchronized(this.writeSync) {
            val array = this.subscribers[type]

            if (array == null) {
                this.subscribers[type] = arrayOf(handler)
            } else {
                if (handler in array) {
                    return
                }

                val newArray = arrayOfNulls<Handler<T>>(array.size + 1)

                val index = Arrays.binarySearch(array, handler).let { i ->
                    if (i < 0) {
                        -i - 1
                    } else {
                        i
                    }
                }

                System.arraycopy(array, 0, newArray, 0, index)
                newArray[index] = handler
                System.arraycopy(array, index, newArray, index + 1, array.size - index)

                @Suppress("UNCHECKED_CAST")
                this.subscribers[type] = newArray as Array<Handler<*>>
            }
        }
    }

    /**
     * Adds a [Handler] to the [subscribers].
     *
     * @param T The type the lambda accepts.
     * @param handler The handler to be added to the [subscribers].
     */
    inline fun <reified T : Any> subscribe(handler: Handler<T>) {
        this.subscribe(T::class, handler)
    }

    /**
     * Adds a lambda to the registry, with a certain priority if desired.
     * Note that this lambda cannot be removed.
     *
     * @param priority The priority which you want this lambda to be added at. Will default to `-50`.
     * @param action Lambda to add to the [subscribers].
     */
    inline fun <reified T : Any> subscribe(priority: Int = -50, noinline action: (T) -> Unit) {
        this.subscribe(T::class, Handler(priority, action))
    }

    /**
     * Removes a [Handler] from the [subscribers] with the [KClass] target specified.
     *
     * @param type Type that the [Handler] will accept.
     * @param handler The [Handler] to add to the [subscribers]
     */
    fun <T : Any> unsubscribe(type: KClass<T>, handler: Handler<T>) {
        synchronized(writeSync) {
            val array = this.subscribers[type]

            if (array != null) {
                if (array.size > 1) {
                    val index = array.indexOf(handler)

                    if (index < 0) {
                        return
                    }

                    val newArray = arrayOfNulls<Handler<T>>(array.size - 1)

                    System.arraycopy(array, 0, newArray, 0, index)
                    System.arraycopy(array, index + 1, newArray, index, array.size - index - 1)

                    @Suppress("UNCHECKED_CAST")
                    this.subscribers[type] = newArray as Array<Handler<*>>
                } else {
                    this.subscribers.remove(type)
                }
            }
        }
    }

    /**
     * Removes a [Handler] from the [subscribers].
     *
     * @param handler The handler to remove from the [subscribers].
     */
    inline fun <reified T : Any> unsubscribe(handler: Handler<T>) {
        this.unsubscribe(T::class, handler)
    }

    /**
     * For dispatching events.
     *
     * @see <a href="https://github.com/x4e/EventDispatcher/">cookiedragon event bus for my inspiration</a>.
     *
     * @param event Event to be posted to all registered actions.
     */
    fun post(event: Any) {
        this.subscribers[event::class]?.forEach { handler ->
            handler.callback(event)
        }

        if (this.recursive) {
            var clazz: Class<*>? = event.javaClass.superclass

            while (clazz != null) {
                this.subscribers[clazz.kotlin]?.forEach { handler ->
                    handler.callback(event)
                }
                clazz = clazz.superclass
            }
        }
    }

    class Handler<T>(private val priority: Int = -50, callback: (T) -> Unit) : Comparable<Handler<*>> {
        // This is dumb, but it improves posting performance which is paramount.
        @Suppress("UNCHECKED_CAST")
        internal val callback = callback as (Any) -> Unit

        override operator fun compareTo(other: Handler<*>): Int {
            return -this.priority.compareTo(other.priority)
        }
    }
}