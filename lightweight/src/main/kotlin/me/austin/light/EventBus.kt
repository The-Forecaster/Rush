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
    private val registry = mutableMapOf<KClass<*>, Array<Handler<*>>>()

    /**
     * This is so we only ever have 1 write action going on at a time.
     */
    private val writeSync = Any()

    /**
     * Adds a [Handler] to the [registry].
     *
     * @param T The type the lambda accepts.
     * @param handler The handler to be added to the [registry].
     */
    inline fun <reified T : Any> register(handler: Handler<T>) {
        this.register(T::class, handler)
    }

    /**
     * Adds a lambda to the registry, with a certain priority if desired.
     * Note that this lambda cannot be removed.
     *
     * @param priority The priority which you want this lambda to be added at. Will default to `-50`.
     * @param action Lambda to add to the [registry].
     */
    inline fun <reified T : Any> register(priority: Int = -50, noinline action: (T) -> Unit) {
        this.register(Handler(priority, action))
    }

    /**
     * Adds a [Handler] to the registry with the [KClass] target explicitly stated.
     *
     * @param type Type that the [Handler] will accept.
     * @param handler The [Handler] to add to the [registry]
     */
    fun <T : Any> register(type: KClass<T>, handler: Handler<T>) {
        synchronized(this.writeSync) {
            val array = this.registry[type]

            this.registry[type] = if (array == null) {
                arrayOf(handler)
            } else if (array.contains(handler)) {
                array
            } else {
                val newArray = arrayOfNulls<Handler<T>>(array.size + 1)

                val index = Arrays.binarySearch(array, handler).let {
                    // Doing this in case something goes wrong
                    if (it < 0) {
                        -it - 1
                    } else {
                        it
                    }
                }

                System.arraycopy(array, 0, newArray, 0, index)
                newArray[index] = handler
                System.arraycopy(array, index, newArray, index + 1, array.size - index)

                newArray as Array<Handler<*>>
            }
        }
    }

    /**
     * Removes a [Handler] from the [registry].
     *
     * @param handler The handler to remove from the [registry].
     */
    inline fun <reified T : Any> unregister(handler: Handler<T>) {
        this.unregister(T::class, handler)
    }

    /**
     * Removes a [Handler] from the [registry] with the [KClass] target specified.
     *
     * @param type Type that the [Handler] will accept.
     * @param handler The [Handler] to add to the [registry]
     */
    fun <T : Any> unregister(type: KClass<T>, handler: Handler<T>) {
        synchronized(writeSync) {
            // This might fuck with it, but I'm going to leave it for now
            this.registry[type]?.let { array ->
                if (array.size > 1) {
                    val newArray = arrayOfNulls<Handler<T>>(array.size - 1)
                    val index = array.indexOf(handler)

                    if (index < 0) {
                        return
                    }

                    System.arraycopy(array, 0, newArray, 0, index)
                    System.arraycopy(array, index + 1, newArray, index, array.size - index - 1)

                    this.registry[type] = newArray as Array<Handler<*>>
                } else {
                    this.registry.remove(type)
                }
            }
        }
    }

    /**
     * For dispatching events.
     *
     * @see <a href="https://github.com/x4e/EventDispatcher/">cookiedragon event bus for my inspiration</a>.
     *
     * @param event Event to be posted to all registered actions.
     */
    fun post(event: Any) {
        this.registry[event::class]?.forEach { handler ->
            handler.callback(event)
        }

        if (this.recursive) {
            var clazz: Class<*>? = event.javaClass.superclass

            while (clazz != null) {
                this.registry[clazz.kotlin]?.forEach { handler ->
                    handler.callback(event)
                }
                clazz = clazz.superclass
            }
        }
    }

    class Handler<T>(private val priority: Int = -50, callback: (T) -> Unit) : Comparable<Handler<*>> {
        // This is dumb, but it improves posting performance which is paramount.
        internal val callback = callback as (Any) -> Unit

        override operator fun compareTo(other: Handler<*>): Int {
            return -this.priority.compareTo(other.priority)
        }
    }
}