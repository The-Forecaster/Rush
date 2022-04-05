package me.austin.rush.bus

import me.austin.rush.listener.Listener

/**
 * Basic structure for an event dispatcher
 *
 * @author Austin
 */
interface EventBus {
    val registry: MutableMap<Class<*>, MutableSet<Listener<*>>>

    /**
     * Adds the Subscriber to the registry
     *
     * @param subscriber event Subscriber instance
     */
    fun register(subscriber: Any)

    /**
     * Removes the Subscriber from the registry
     *
     * @param subscriber event subscriber instance
     */
    fun unregister(subscriber: Any)

    /**
     * Check if an object is currently in the registry
     *
     * @return if the object is in the registry
     */
    fun isRegistered(subscriber: Any): Boolean

    /**
     * Post an event to be processed by the subscribed methods or listener objects
     *
     * @param <T> event type
     * @param event object to post
     *
     * @return the event you passed
    </T> */
    fun <T> dispatch(event: T): T
}