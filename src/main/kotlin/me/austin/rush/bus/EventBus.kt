package me.austin.rush.bus

import me.austin.rush.listener.Listener
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
     * @return true if the listener was added, false if it was already present
     */
    fun register(listener: Listener<*>)

    /**
     * Adds all listeners to the registry
     *
     * @param listeners all listeners you want to be added
     * @return true if all listeners were added, false if any weren't
     */
    fun registerAll(vararg listeners: Listener<*>) {
        for (listener in listeners) this.register(listener)
    }

    /**
     * Adds all listeners in an iterable to the registry
     *
     * @param listeners the iterable of listeners you want to be added
     * @return true if all listeners were added, false if any weren't
     */
    fun registerAll(listeners: Iterable<Listener<*>>) {
        for (listener in listeners) this.register(listener)
    }

    /**
     * Removes the listener from the registry
     *
     * @param listener listener object to be removed
     * @return true if it was removed, false if it couldn't be removed or target isn't present in the registry
     */
    fun unregister(listener: Listener<*>)

    /**
     * Removes all listeners from the registry
     *
     * @param listeners listener objects you want to be removed
     * @see unregister
     * @return true if all were removed, false if any could not be removed
     */
    fun unregisterAll(vararg listeners: Listener<*>) {
        for (listener in listeners) this.unregister(listener)
    }

    /**
     * Removes all listeners in an iterable from the registry
     *
     * @param listeners iterable of listeners you want to be removed
     * @see unregister
     * @return true if all were removed, false if any could not be
     */
    fun unregisterAll(listeners: Iterable<Listener<*>>) {
        for (listener in listeners) this.unregister(listener)
    }

    /**
     * Adds all annotated listeners into the registry
     *
     * @param subscriber object you want to be searched for listeners to be added to the registry
     * @return true if all listeners inside the class could be registered, false if any could not
     */
    fun register(subscriber: Any)

    /**
     * Adds all objects and their contained listeners to the registry
     *
     * @param subscribers all subscribers you want to be added to the registry
     * @return true if all listeners inside all the objects could be registered, false if any could not
     */
    fun registerAll(vararg subscribers: Any) {
        for (subscriber in subscribers) this.register(subscriber)
    }

    /**
     * Removes all annotated listeners from the registry
     *
     * @param subscriber event subscriber instance
     * @return true if all listeners inside this object could be removed from the registry, false if any could not
     */
    fun unregister(subscriber: Any)

    /**
     * Removes all objects and their contained listeners to the registry
     *
     * @param subscribers all subscribers you want removed from the registry
     * @return true if all listeners inside all the objects could be registered, false if any could not
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
    fun <T : Any> dispatch(event: T): T
}