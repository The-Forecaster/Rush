package me.austin.light

import java.util.*
import kotlin.reflect.KClass

/**
 * Extremely lightweight event-bus made for kotlin use.
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
    private val registry = HashMap<KClass<*>, Array<Handler>>()

    /**
     * This is so we only ever have 1 write action going on at a time.
     */
    private val writeSync = Any()

    /**
     * Adds a lambda to the [registry].
     *
     * @param T The type the lambda accepts.
     * @param handler The handler to be added to the registry.
     */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE", "UNCHECKED_CAST")
    inline fun <reified T : Any> register(handler: Handler) {
        synchronized(this.writeSync) {
            val array = this.registry[T::class]

            this.registry[T::class] = if (array == null) {
                arrayOf(handler)
            } else if (array.contains(handler)) {
                array
            } else {
                when (array.size) {
                    1 -> {
                        arrayOf(array[0], handler)
                    }

                    2 -> {
                        arrayOf(array[0], array[1], handler)
                    }

                    3 -> {
                        arrayOf(array[0], array[1], array[2], handler)
                    }

                    else -> {
                        val out = Array<Handler?>(array.size + 1) { null }

                        val index = Arrays.binarySearch(array, handler).let {
                            if (it < 0) {
                                -it - 1
                            } else {
                                it
                            }
                        }

                        System.arraycopy(array, 0, out, 0, index)
                        System.arraycopy(array, index, out, index + 1, array.size - index)
                        out[index] = handler

                        out
                    }
                }
            } as Array<Handler>
        }
    }

    inline fun <reified T : Any> register(priority: Int = -50, noinline action: (T) -> Unit) {
        this.register<T>(Handler(priority, action))
    }

    /**
     * Removes the specified action from the [registry].
     *
     * @param handler The handler to remove from the registry.
     */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE", "UNCHECKED_CAST")
    inline fun <reified T : Any> unregister(handler: Handler) {
        synchronized(writeSync) {
            val array = this.registry[T::class]

            if (array != null) {
                if (array.size > 1) {
                    val out = Array<Handler?>(array.size - 1) { null }
                    var index = 0

                    for (element in array) {
                        if (handler != element) {
                            out[index] = element
                            index++
                        }
                    }

                    this.registry[T::class] = out as Array<Handler>
                } else {
                    this.registry.remove(T::class)
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
        this.registry[event::class]?.let {
            for (handler in it) {
                handler.invoker(event)
            }
        }

        if (recursive) {
            var clazz: Class<*>? = event.javaClass.superclass

            while (clazz != null) {
                this.registry[clazz.kotlin]?.let {
                    for (handler in it) {
                        handler.invoker(event)
                    }
                }
                clazz = clazz.superclass
            }
        }
    }

    class Handler(private val priority: Int, handle: (Nothing) -> Unit) : Comparable<Handler> {
        internal val invoker = handle as (Any) -> Unit

        override fun compareTo(other: Handler): Int {
            return -this.priority.compareTo(other.priority)
        }
    }
}