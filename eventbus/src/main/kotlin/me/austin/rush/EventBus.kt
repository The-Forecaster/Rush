package me.austin.rush;

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * Basic structure for an event dispatcher
 *
 * @author Austin
 * @since 2022
 */
interface IEventBus {
    /**
     * Map that will be used to store registered listeners and their targets
     *
     * The key-set will hold all stored targets of listeners
     * The value-set will hold the list of listeners corresponding to their respective targets
     */
    val registry: MutableMap<KClass<*>, MutableList<IListener<*>>>

    /**
     * Adds the listener into the registry
     *
     * @param listener instance of listener<T> to subscribe
     */
    fun register(listener: IListener<*>)

    /**
     * Adds all listeners to the registry
     *
     * @param listeners all listeners you want to be added
     */
    fun registerAll(vararg listeners: IListener<*>) {
        for (listener in listeners) this.register(listener)
    }

    /**
     * Adds all listeners in an iterable to the registry
     *
     * @param listeners the iterable of listeners you want to be added
     */
    fun registerAll(listeners: Iterable<IListener<*>>) {
        for (listener in listeners) this.register(listener)
    }

    /**
     * Removes the listener from the registry
     *
     * @param listener listener object to be removed
     */
    fun unregister(listener: IListener<*>)

    /**
     * Removes all listeners from the registry
     *
     * @param listeners listener objects you want to be removed
     * @see unregister
     */
    fun unregisterAll(vararg listeners: IListener<*>) {
        for (listener in listeners) this.unregister(listener)
    }

    /**
     * Removes all listeners in an iterable from the registry
     *
     * @param listeners iterable of listeners you want to be removed
     * @see unregister
     */
    fun unregisterAll(listeners: Iterable<IListener<*>>) {
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
     */
    fun <T : Any> dispatch(event: T)
}

/**
 * Basic implementation of [IEventBus]
 *
 * @author Austin
 * @since 2022
 */
open class EventBus : IEventBus {
    override val registry = ConcurrentHashMap<KClass<*>, MutableList<IListener<*>>>()

    // Using this here, so we don't have to make more reflection calls
    private val cache = ConcurrentHashMap<Any, List<IListener<*>>>()

    override fun register(listener: IListener<*>) {
        this.registry.getOrPut(listener.target, ::CopyOnWriteArrayList).let {
            // For if a listener is already registered
            if (it.contains(listener)) return

            var index = 0

            while (index < it.size) {
                if (it[index].priority < listener.priority) break

                index++
            }

            it.add(index, listener)
        }
    }

    override fun unregister(listener: IListener<*>) {
        this.registry[listener.target]?.remove(listener)
    }

    override fun register(subscriber: Any) {
        for (listener in this.cache.getOrPut(subscriber, subscriber::listeners)) this.register(listener)
    }

    override fun unregister(subscriber: Any) {
        for (listener in subscriber.listeners) this.unregister(listener)
    }

    override fun <T : Any> dispatch(event: T) {
        this.listWith(event) {
            synchronized(it) {
                for (listener in it) {
                    listener(event)
                }
            }
        }
    }

    /**
     * Dispatches an event that is cancellable.
     * When the event is cancelled it will not be posted to any listeners after
     *
     * @param event the event which will be posted
     * @return the event passed through
     */
    fun <T : Cancellable> dispatch(event: T): T {
        this.listWith(event) {
            // This could cause some problems, but we probably want this for thread-safety
            synchronized(it) {
                for (listener in it) {
                    listener(event)
                    if (event.isCancelled) break
                }
            }
        }

        return event
    }

    private fun <T : Any> listWith(event: T, block: (MutableList<IListener<T>>) -> Unit) {
        (registry[event::class] as? MutableList<IListener<T>>)?.let {
            (block(it))
        }
    }
}