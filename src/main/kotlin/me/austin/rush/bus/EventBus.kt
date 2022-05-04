package me.austin.rush.bus

import me.austin.rush.listener.Listener
import kotlin.reflect.KClass

/**
 * Basic structure for an event dispatcher
 *
 * @author Austin
 */
interface EventBus {
    val registry: MutableMap<KClass<*>, MutableList<Listener<*>>>

    /**
     * Adds the listener into the registry
     *
     * @param listener instance of listener<T> to subscribe
     */
    fun register(listener: Listener<*>)

    /**
     * Removes the listener into the registry
     *
     * @param listener listener object to unsubscribe
     */
    fun unregister(listener: Listener<*>)

    /**
     * Adds all annotated listeners into the registry
     *
     * @param subscriber event subscriber instance
     */
    fun register(subscriber: Any)

    /**
     * Removes all annotated listeners from the registry
     *
     * @param subscriber event subscriber instance
     */
    fun unregister(subscriber: Any)

    /**
     * Post an event to be processed by the subscribed methods or listener objects
     *
     * @param <T> event type
     * @param event object to post
     *
     * @return the event you passed
    </T> */
    fun <T : Any> dispatch(event: T): T
}
