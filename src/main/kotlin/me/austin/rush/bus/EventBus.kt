package me.austin.rush.bus

import me.austin.rush.listener.Listener
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
    fun register(listener: Listener<*>)

    /**
     *
     */
    fun registerAll(vararg listeners: Listener<*>) {
        for (listener in listeners) this.register(listener)
    }

    fun registerAll(listeners: Iterable<Listener<*>>) = Stream.of(listeners).forEach(::register)

    /**
     * Removes the listener into the registry
     *
     * @param listener listener object to unsubscribe
     */
    fun unregister(listener: Listener<*>)

    fun unregisterAll(vararg listeners: Listener<*>) {
        for (listener in listeners) this.unregister(listener)
    }

    fun unregisterAll(listeners: Iterable<Listener<*>>) = listeners.forEach(::unregister)

    /**
     * Adds all annotated listeners into the registry
     *
     * @param subscriber event subscriber instance
     */
    fun register(subscriber: Any)

    fun register(vararg subscribers: Any) {
        for (subscriber in subscribers) this.register(subscriber)
    }

    /**
     * Removes all annotated listeners from the registry
     *
     * @param subscriber event subscriber instance
     */
    fun unregister(subscriber: Any)

    fun unregisterAll(vararg subscribers: Any) {
        for (subscriber in subscribers) this.register(subscriber)
    }

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
