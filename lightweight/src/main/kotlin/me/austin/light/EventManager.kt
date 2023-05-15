package me.austin.light

import java.util.*
import kotlin.reflect.KClass

/**
 * Extremely lightweight event-bus made for kotlin use
 *
 * @author Austin
 * @since 2023
 *
 * @param recursive if the bus will also post superclasses of events posted
 */
open class EventManager(open val recursive: Boolean) {
    /**
     * For all the classes of events and the lambdas which target them
     */
    open val registry = mutableMapOf<KClass<*>, MutableList<Handler>>()

    /**
     * Add a static lambda to the [registry]
     *
     * @param T the type the lambda accepts
     * @param action the lambda to be added
     */
    inline fun <reified T> register(noinline action: (T) -> Unit) {
        this.registry.getOrPut(T::class) { LinkedList() }.let {
            synchronized(it) { it.add(Handler(action, this)) }
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
     * Remove all [Handler] objects from the list that have the parameter as their parent
     *
     * @param parent the object search for and remove listeners of
     */
    fun unregister(parent: Any) {
        for ((target, list) in this.registry) {
            synchronized(list) {
                list.removeIf {
                    it.parent == parent
                }

                // This improves posting performance which is more important
                if (list.isEmpty()) {
                    this.registry.remove(target)
                }
            }
        }
    }

    /**
     * For dispatching events
     *
     * @param event event to be posted to all registered actions
     */
    open fun post(event: Any) {
        for (clazz in event::class.getClasses(this.recursive)) {
            this.registry[clazz]?.let {
                for (handler in it) {
                    handler.action(event)
                }
            }
        }
    }

    /**
     * For containing a listener and the object it's contained inside
     *
     * @param lambda action to be invoked
     * @param parent object to be checked when removing listeners
     */
    open class Handler(lambda: (Nothing) -> Unit, val parent: Any) {
        /**
         * Action to be invoked
         */
        open val action = lambda as (Any) -> Unit
    }
}

/**
 * This is copied from [<a href="https://github.com/x4e/EventDispatcher/">cookiedragon</a>]
 *
 * @return immutable list of the class and all of its superclasses in order
 */
internal fun KClass<*>.getClasses(recursive: Boolean): List<KClass<*>> {
    val classes = mutableListOf(this)
    var clazz: Class<*>? = this.java.superclass
    while (recursive && clazz != null) {
        classes.add(clazz.kotlin)
        clazz = clazz.superclass
    }
    return classes.toList()
}