package me.austin.rush.bus

import me.austin.rush.listener.Listener
import java.util.Arrays
import java.util.stream.Stream
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
    fun register(listener: Listener<*>): Boolean

    /**
     *
     */
    fun registerAll(vararg listeners: Listener<*>) = listeners.map(::register).all()

    fun registerAll(listeners: Iterable<Listener<*>>) = listeners.map(::register).all()

    /**
     * Removes the listener from the registry
     *
     * @param listener listener object to unsubscribe
     */
    fun unregister(listener: Listener<*>): Boolean

    fun unregisterAll(vararg listeners: Listener<*>) = listeners.map(::register).all()

    fun unregisterAll(listeners: Iterable<Listener<*>>) = listeners.map(::register).all()

    /**
     * Adds all annotated listeners into the registry
     *
     * @param subscriber event subscriber instance
     */
    fun register(subscriber: Any) : Boolean

    fun registerAll(vararg subscribers: Any) = subscribers.map(::register).all()

    /**
     * Removes all annotated listeners from the registry
     *
     * @param subscriber event subscriber instance
     */
    fun unregister(subscriber: Any) : Boolean

    fun unregisterAll(vararg subscribers: Any) = subscribers.map(::register).all()

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

internal fun Iterable<Boolean>.all() = this.all { it }
