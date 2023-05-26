package me.austin.light

import kotlin.reflect.KClass

/**
 * Extremely lightweight event-bus made for kotlin use
 *
 * @author Austin
 * @since 2023
 *
 * @param recursive if the bus will also post superclasses of events posted
 */
class EventManager(private val recursive: Boolean = true) {
    /**
     * For all the classes of events and the lambdas which target them
     */
    val registry = HashMap<KClass<*>, Array<out (Any) -> Unit>>()

    /**
     * This is so we only ever have 1 write action going on at a time
     */
    val writeSync = Any()

    /**
     * Adds a lambda to the [registry]
     *
     * @param T the type the lambda accepts
     * @param action the lambda to be added
     */
    inline fun <reified T : Any> register(noinline action: (T) -> Unit) {
        synchronized(writeSync) {
            val array = registry[T::class]

            if (array === null) {
                registry[T::class] = arrayOf(action) as Array<out (Any) -> Unit>
            } else if (!array.contains(action)) {
                registry[T::class] = when (array.size) {
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
                        val fin = Array<((T) -> Unit)?>(array.size - 1) { null }

                        for (i in array.indices) {
                            fin[i] = array[i]
                        }

                        fin[fin.size - 1] = action
                        fin
                    }
                } as Array<out (Any) -> Unit>
            }
        }
    }

    /**
     * Removes the specified action from the [registry]
     *
     * @param action the object search for and remove listeners of
     */
    inline fun <reified T : Any> unregister(noinline action: (T) -> Unit) {
        synchronized(writeSync) {
            val array = registry[T::class]

            if (array != null) {
                if (array.size > 1) {
                    val fin = Array<((T) -> Unit)?>(array.size - 1) { null }
                    var index = 0

                    for (element in array) {
                        if (action != element) {
                            fin[index] = element
                            index++
                        }
                    }

                    registry[T::class] = fin as Array<out (Any) -> Unit>
                }
            }
        }
    }

    /**
     * For dispatching events
     *
     * @see <a href="https://github.com/x4e/EventDispatcher/">cookiedragon event bus for my inspiration</a>
     *
     * @param event event to be posted to all registered actions
     */
    fun post(event: Any) {
        registry[event::class]?.let {
            for (action in it) {
                action(event)
            }
        }
        if (recursive) {
            var clazz = event.javaClass.superclass
            while (clazz != null) {
                registry[event::class]?.let {
                    for (action in it) {
                        action(event)
                    }
                }
                clazz = clazz.superclass
            }
        }
    }
}