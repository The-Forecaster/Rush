package me.austin.rush

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
        for (listener in listeners) {
            this.register(listener)
        }
    }

    /**
     * Adds all listeners in an iterable to the registry
     *
     * @param listeners the iterable of listeners you want to be added
     */
    fun registerAll(listeners: Iterable<Listener<*>>) {
        for (listener in listeners) {
            this.register(listener)
        }
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
        for (listener in listeners) {
            this.unregister(listener)
        }
    }

    /**
     * Removes all listeners in an iterable from the registry
     *
     * @param listeners iterable of listeners you want to be removed
     * @see unregister
     */
    fun unregisterAll(listeners: Iterable<Listener<*>>) {
        for (listener in listeners) {
            this.unregister(listener)
        }
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
        for (subscriber in subscribers) {
            this.register(subscriber)
        }
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
        for (subscriber in subscribers) {
            this.unregister(subscriber)
        }
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
class EventBus : IEventBus {
    /**
     * Map that will be used to store registered listeners and their targets
     *
     * The key-set will hold all stored targets of listeners
     * The value-set will hold the list of listeners corresponding to their respective targets
     */
    private val registry = ConcurrentHashMap<KClass<*>, MutableList<Listener<*>>>()

    // Using this here, so we don't have to make more reflection calls
    private val cache = ConcurrentHashMap<Any, List<Listener<*>>>()

    override fun register(listener: Listener<*>) {
        this.registry.getOrPut(listener.target, ::CopyOnWriteArrayList).let {
            synchronized(it) {
                if (it.contains(listener)) return

                var index = 0

                while (index < it.size) {
                    if (it[index].priority < listener.priority) break

                    index++
                }

                it.add(index, listener)
            }
        }
    }

    override fun unregister(listener: Listener<*>) {
        this.registry[listener.target]?.let {
            synchronized(it) {
                it.remove(listener)
            }
        }
    }

    override fun register(subscriber: Any) {
        for (listener in this.cache.getOrPut(subscriber, subscriber::listeners)) {
            this.register(listener)
        }
    }

    override fun unregister(subscriber: Any) {
        for (listener in subscriber.listeners) {
            this.unregister(listener)
        }
    }

    override fun <T : Any> dispatch(event: T) {
        this.listWith(event) {
            for (listener in it) {
                listener(event)
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
            for (listener in it) {
                listener(event)

                if (event.isCancelled) {
                    break
                }
            }
        }

        return event
    }

    /**
     * For removing code duplication
     *
     * @param event event to call from [registry]
     * @param block the code block to call if the list exists
     */
    private fun <T : Any> listWith(event: T, block: (MutableList<Listener<T>>) -> Unit) {
        (registry[event::class] as? MutableList<Listener<T>>)?.let {
            block(it)
        }
    }
}