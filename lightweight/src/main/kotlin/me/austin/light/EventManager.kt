package me.austin.light

import java.util.*
import kotlin.reflect.KClass

/**
 * Extremely lightweight event-bus made for kotlin use
 *
 * @author Austin
 * @since 2023
 */
open class EventManager {
    /**
     * For all the classes of events and the lambdas which target them
     */
    open val registry = mutableMapOf<KClass<*>, MutableList<Handler>>()

    /**
     * Add a lambda to the [registry]
     *
     * @param T the type the lambda accepts
     * @param action the lambda to be added
     */
    inline fun <reified T> register(noinline action: (T) -> Unit) {
        this.registry.getOrPut(T::class) { LinkedList() }.let {
            synchronized(it) { it.add(Handler(action)) }
        }
    }

    /**
     * Add a [Handler] to the [registry]
     *
     * @param T the action the handler will accept
     * @param handler the handler object to add to the registry
     */
    inline fun <reified T> register(handler: Handler) {
        this.registry.getOrPut(T::class) { LinkedList() }.let {
            synchronized(it) { it.add(handler) }
        }
    }

    /**
     * For dispatching events
     *
     * @param event event to be posted to all registered actions
     */
    open fun post(event: Any) {
        for (clazz in event::class.allClasses) {
            this.registry[clazz]?.let {
                synchronized(it) { for (handler in it) if (handler.active) handler.action(event) }
            }
        }
    }

    /**
     * For containing a listener and if its active
     *
     * If you wish for a listener to no longer accept events then just set active to false
     */
    class Handler(lambda: (Nothing) -> Unit, var active: Boolean = true) {
        val action = lambda as (Any) -> Unit
    }
}


/**
 * This is stolen from [<a href="https://github.com/x4e/EventDispatcher/">cookiedragon</a>]
 *
 * @return immutable list of the class and all of its superclasses in order
 */
internal val KClass<*>.allClasses: List<KClass<*>>
    get() {
        val classes = mutableListOf(this)
        var clazz: Class<*>? = java.superclass
        while (clazz != null) {
            classes.add(clazz.kotlin)
            clazz = clazz.superclass
        }
        return classes.toList()
    }