package me.austin.light

import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
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
            val array = this.registry[T::class] as? Array<out (T) -> Unit>

            if (array == null) {
                this.registry[T::class] = arrayOf(action) as Array<out (Any) -> Unit>
            } else if (!array.contains(action)) {
                this.registry[T::class] = when(array.size) {
                    1 -> arrayOf(array[0], action)
                    2 -> arrayOf(array[0], array[1], action)
                    else -> {
                        val fin = arrayOfNulls<(T) -> Unit>(array.size + 1)

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
            this.registry[T::class]?.let {
                if (it.contains(action)) {
                    if (it.size == 1) {
                        this.registry.remove(T::class)
                    } else {
                        val array = Array<((T) -> Unit)?>(it.size - 1) { null }
                        var index = 0

                        for (element in it) {
                            if (action != element) {
                                array[index] = element
                                index++
                            }
                        }

                        this.registry[T::class] = array as Array<out (Any) -> Unit>
                    }
                }
            }
        }
    }

    /**
     * For dispatching events
     *
     * @param event event to be posted to all registered actions
     */
    fun post(event: Any) {
        this.registry[event::class]?.let {
            for (action in it) {
                action(event)
            }
        }

        if (this.recursive) {
            var clazz: Class<*>? = event.javaClass.superclass

            while (clazz != null) {
                this.registry[event::class]?.let {
                    for (action in it) {
                        action(event)
                    }
                }
                clazz = clazz.superclass
            }
        }
    }

    /**
     * This is copied from [<a href="https://github.com/x4e/EventDispatcher/">cookiedragon</a>]
     * @param recursive if we should also include superclasses of the class
     * @return immutable list of the class and all of its superclasses in order if [recursive] is set to true
     */
    private fun KClass<*>.getClasses(recursive: Boolean): List<KClass<*>> {
        val classes = mutableListOf(this)
        var clazz: Class<*>? = this.java.superclass
        while (recursive && clazz != null) {
            classes.add(clazz.kotlin)
            clazz = clazz.superclass
        }
        return classes.toList()
    }
}