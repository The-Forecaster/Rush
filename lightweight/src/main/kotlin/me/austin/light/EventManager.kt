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
    open val registry = linkedMapOf<KClass<*>, LinkedList<Handler>>()

    /**
     * Add a lambda to the [registry]
     *
     * @param T the type the lambda accepts
     * @param action the lambda to be added
     */
    inline fun <reified T> register(noinline action: (T) -> Unit) {
        this.registry.getOrPut(T::class) { LinkedList() }.let {
            synchronized(it) {
                it.add(Handler(action))
            }
        }
    }

    inline fun <reified T> register(noinline action: (T) -> Unit, parent: Any) {
        this.registry.getOrPut(T::class) { LinkedList() }.let {
            synchronized(it) { it.add(Handler(action, parent)) }
        }
    }

    inline fun <reified T> unregister(parent: Any) {
        this.registry[T::class]?.let { list ->
            synchronized(list) { list.removeIf { it.parent == parent } }
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
                synchronized(it) { for (handler in it) handler.action(event) }
            }
        }
    }

    class Handler(lambda: (Nothing) -> Unit, val parent: Any? = null) {
        val action = lambda as (Any) -> Unit
    }
}


/**
 * This is stolen from [<a href="https://github.com/x4e/EventDispatcher/">cookiedragon</a>]
 *
 * @return the class and all of its superclasses in order
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