package me.austin.rush

import kotlin.reflect.KClass

/**
 * Basic structure for an event dispatcher
 *
 * @author Austin
 */
interface EventBus {
    /**
     * Map that will be used to store registered listeners and their targets
     *
     * The key-set will hold all stored targets of listeners
     * The value-set will hold the list of listeners corresponding to their respective targets
     */
    val registry: MutableMap<KClass<*>, MutableList<Listener<*>>>

    /**
     * Adds the listener into the registry
     *
     * @param listener instance of listener<T> to subscribe
     */
    fun register(listener: Listener<*>)

    /**
     * Adds all listeners to the registry
     *
     * @param listeners all listeners you want to be added
     */
    fun registerAll(vararg listeners: Listener<*>) {
        for (listener in listeners) this.register(listener)
    }

    /**
     * Adds all listeners in an iterable to the registry
     *
     * @param listeners the iterable of listeners you want to be added
     */
    fun registerAll(listeners: Iterable<Listener<*>>) {
        for (listener in listeners) this.register(listener)
    }

    /**
     * Removes the listener from the registry
     *
     * @param listener listener object to be removed
     */
    fun unregister(listener: Listener<*>)

    /**
     * Removes all listeners from the registry
     *
     * @param listeners listener objects you want to be removed
     * @see unregister
     */
    fun unregisterAll(vararg listeners: Listener<*>) {
        for (listener in listeners) this.unregister(listener)
    }

    /**
     * Removes all listeners in an iterable from the registry
     *
     * @param listeners iterable of listeners you want to be removed
     * @see unregister
     */
    fun unregisterAll(listeners: Iterable<Listener<*>>) {
        for (listener in listeners) this.unregister(listener)
    }

    /**
     * Adds all annotated listeners into the registry
     *
     * @param subscriber object you want to be searched for listeners to be added to the registry
     */
    fun register(subscriber: Any)

    /**
     * Adds all objects and their contained listeners to the registry
     *
     * @param subscribers all subscribers you want to be added to the registry
     */
    fun registerAll(vararg subscribers: Any) {
        for (subscriber in subscribers) this.register(subscriber)
    }

    /**
     * Removes all annotated listeners from the registry
     *
     * @param subscriber event subscriber instance
     */
    fun unregister(subscriber: Any)

    /**
     * Removes all objects and their contained listeners to the registry
     *
     * @param subscribers all subscribers you want removed from the registry
     */
    fun unregisterAll(vararg subscribers: Any) {
        for (subscriber in subscribers) this.unregister(subscriber)
    }

    /**
     * Post an event to be processed by the subscribed methods or listener objects
     *
     * @param <T> event type
     * @param event object to post
     *
     * @return the event you passed
     */
    fun <T : Any> dispatch(event: T)
}

/**
 * Basic structure for an event listener and invoker.
 *
 * @author Austin
 */
interface Listener<T : Any> {

    /** the class of the target event */
    val target: KClass<T>

    /** the priority that the listener will be called upon(use wisely) */
    val priority: Int

    /**
     * Processes an event passed through this listener
     *
     * @param param event object that is being processed
     */
    operator fun invoke(param: T)
}

abstract class Cancellable {
    var isCancelled = false

    fun cancel() {
        this.isCancelled = true
    }
}