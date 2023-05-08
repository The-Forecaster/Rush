package me.austin.light

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

/**
 * Extremely lightweight event-bus made for kotlin use
 *
 * @author Austin
 */
open class EventManager {
    /**
     * For all the classes of events and the lambdas which target them
     */
    open val registry = LinkedHashMap<KClass<*>, MutableList<Handler>>()

    /**
     * Add a lambda to the [registry]
     *
     * @param T the type the lambda accepts
     * @param action the lambda to be added
     */
    inline fun <reified T : Any> Any.register(noinline action: (T) -> Unit) {
        registry.getOrPut(T::class) { LinkedList() }.add(Handler(action as (Any) -> Unit, this, true))
    }

    /**
     * Remove a lambda from the registry
     *
     * @param T the type that the lambda accepts
     * @param action the lambda to be removed
     */
    inline fun <reified T : Any> Any.unregister(noinline action: (T) -> Unit) {
        for (handler in registry[T::class] ?: return) if (handler.action == action) handler.enabled = false
    }

    fun unregister(parent: Any) {
        // This kinda sucks but I otherwise we'd have to use reflection which I don't want to do for this
        for (list in registry.values) list.removeIf { it.parent == parent }
    }

    /**
     * For dispatching events
     *
     * @param event event to be posted to all registered actions
     */
    fun post(event: Any) {
        for (clazz in (listOf(event::class) + event::class.allSuperclasses)) {
            this.registry[clazz]?.let {
                synchronized(it) { for (handler in it) handler.action.invoke(event) }
            }
        }
    }

    data class Handler(val action: (Any) -> Unit, val parent: Any, var enabled: Boolean = false)
}