package me.austin.light

import kotlin.reflect.KClass

/**
 * Extremely lightweight event-bus made for kotlin use.
 *
 * @author Austin
 * @since 2023
 *
 * @param recursive If the bus will also post superclasses of events posted.
 */
class EventManager(private val recursive: Boolean = true) {
    /**
     * For all the classes of events and the lambdas which target them.
     */
    private val registry = HashMap<KClass<*>, Array<out (Any) -> Unit>>()

    /**
     * This is so we only ever have 1 write action going on at a time.
     */
    private val writeSync = Any()

    /**
     * Adds a lambda to the [registry].
     *
     * @param T The type the lambda accepts.
     * @param action The lambda to be added.
     */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T : Any> register(noinline action: (T) -> Unit) {
        synchronized(this.writeSync) {
            val array = this.registry[T::class]

            if (array == null) {
                this.registry[T::class] = arrayOf(action) as Array<out (Any) -> Unit>
            } else if (!array.contains(action)) {
                this.registry[T::class] = when (array.size) {
                    1 -> {
                        arrayOf(array[0], action)
                    }

                    2 -> {
                        arrayOf(array[0], array[1], action)
                    }

                    3 -> {
                        arrayOf(array[0], array[1], array[2], action)
                    }

                    else -> {
                        val out = Array<((T) -> Unit)?>(array.size + 1) { null }

                        System.arraycopy(array, 0, out, 0, array.size)

                        out[out.size - 1] = action
                        out
                    }
                } as Array<out (Any) -> Unit>
            }
        }
    }

    /**
     * Removes the specified action from the [registry].
     *
     * @param action The lambda to remove from the registry.
     */
    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    inline fun <reified T : Any> unregister(noinline action: (T) -> Unit) {
        synchronized(writeSync) {
            val array = this.registry[T::class]

            if (array != null) {
                if (array.size > 1) {
                    val out = Array<((T) -> Unit)?>(array.size - 1) { null }
                    var index = 0

                    for (element in array) {
                        if (action != element) {
                            out[index] = element
                            index++
                        }
                    }

                    this.registry[T::class] = out as Array<out (Any) -> Unit>
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
        this.post(event) {
            for (action in it) {
                action(event)
            }
        }
    }

    /**
     * For invoking lambda's on an [Array] from the [registry]
     *
     * @param T Type of the [event].
     * @param event Event to be requested from the [registry].
     * @param block Lambda to be called on the [Array] from the from the [registry].
     */
    fun <T : Any> post(event: T, block: (Array<out (T) -> Unit>) -> Unit) {
        this.registry[event::class]?.let(block)

        if (recursive) {
            var clazz: Class<*>? = event.javaClass.superclass
            while (clazz != null) {
                this.registry[clazz.kotlin]?.let(block)
                clazz = clazz.superclass
            }
        }
    }
}