package me.austin.rush

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

/**
 * Basic structure for an event dispatcher.
 *
 * @author Austin
 * @since 2022
 */
interface IEventBus {
    /**
     * Adds the listener into the registry.
     *
     * @param listener Instance of [Listener] to subscribe.
     */
    fun register(listener: Listener)

    /**
     * Adds all [Listener] objects to the registry.
     *
     * @param listeners All [Listener] objects you want to be added.
     */
    fun registerAll(vararg listeners: Listener) {
        for (listener in listeners) {
            this.register(listener)
        }
    }

    /**
     * Adds all [Listener] objects in an iterable to the registry.
     *
     * @param listeners The [Iterable] of [Listener] objects you want to be added.
     */
    fun registerAll(listeners: Iterable<Listener>) {
        for (listener in listeners) {
            this.register(listener)
        }
    }

    /**
     * Removes the listener from the registry.
     *
     * @param listener [Listener] object to be removed.
     */
    fun unregister(listener: Listener)

    /**
     * Removes all [Listener] objects from the registry.
     *
     * @param listeners [Listener] objects you want to be removed.
     * @see unregister
     */
    fun unregisterAll(vararg listeners: Listener) {
        for (listener in listeners) {
            this.unregister(listener)
        }
    }

    /**
     * Removes all [Listener] objects in an iterable from the registry.
     *
     * @param listeners [Iterable] of [Listener] objects you want to be removed.
     * @see unregister
     */
    fun unregisterAll(listeners: Iterable<Listener>) {
        for (listener in listeners) {
            this.unregister(listener)
        }
    }

    /**
     * Adds all [EventHandler] annotated [Listener] objects into the registry.
     *
     * @param subscriber Object you want to be searched for listeners to be added to the registry.
     */
    fun register(subscriber: Any)

    /**
     * Adds all objects and their contained [EventHandler] annotated [Listener] objects to the registry.
     *
     * @param subscribers All objects you want to be added to the registry.
     */
    fun registerAll(vararg subscribers: Any) {
        for (subscriber in subscribers) {
            this.register(subscriber)
        }
    }

    /**
     * Removes all [EventHandler] annotated [Listener] objects from the registry.
     *
     * @param subscriber Event subscriber instance.
     */
    fun unregister(subscriber: Any)

    /**
     * Removes all objects and their contained [EventHandler] annotated [Listener] objects to the registry.
     *
     * @param subscribers All objects you want removed from the registry.
     */
    fun unregisterAll(vararg subscribers: Any) {
        for (subscriber in subscribers) {
            this.unregister(subscriber)
        }
    }

    /**
     * Post an event to be processed by the subscribed methods or listener objects.
     *
     * @param T Event type.
     * @param event Instance of [T] to post.
     */
    fun <T : Any> dispatch(event: T)
}

/**
 * Basic implementation of [IEventBus].
 *
 * @author Austin
 * @since 2022
 *
 * @param recursive If this eventbus posts superclasses of events posted.
 */
open class EventBus(private val recursive: Boolean = false) : IEventBus {
    /**
     * Map that will be used to store registered [Listener] objects and their targets.
     *
     * The key-set will hold all stored [KClass] targets of [Listener] objects.
     * The value-set will hold the list of [Listener] objects corresponding to their respective targets.
     */
    private val registry = ConcurrentHashMap<KClass<*>, MutableList<Listener>>()

    /**
     * Map that is used to reduce the amount of reflection calls we have to make.
     *
     * The Key set stores an [Object] and the value set hold an [Array] of [Listener] fields in that object.
     */
    private val cache = ConcurrentHashMap<Any, Array<Listener>>()

    /**
     * This is so we only ever have 1 write action going on at a time
     */
    private val writeSync = Any()

    override fun register(listener: Listener) {
        // TODO speed up the registering process
        synchronized(writeSync) {
            val list = this.registry[listener.target]

            if (list == null) {
                this.registry[listener.target] = CopyOnWriteArrayList(arrayOf(listener))
            } else if (!list.contains(listener)) {
                var index = 0

                while (index < list.size) {
                    if (list[index].priority < listener.priority) {
                        break
                    }

                    index++
                }

                list.add(index, listener)

                this.registry[listener.target] = list
            }
        }
    }

    override fun unregister(listener: Listener) {
        // TODO speed up the unregistering process
        synchronized(writeSync) {
            this.registry[listener.target]?.let { list ->
                list.remove(listener)

                this.registry[listener.target] = list

                if (list.size == 0) {
                    this.registry.remove(listener.target)
                }
            }
        }

    }

    override fun register(subscriber: Any) {
        // TODO subscriber.listeners could probably be inlined somewhat
        for (listener in this.cache.getOrPut(subscriber) { subscriber.listeners }) {
            this.register(listener)
        }
    }

    override fun unregister(subscriber: Any) {
        this.cache[subscriber]?.let {
            for (listener in it) {
                this.unregister(listener)
            }
        }
    }

    override fun <T : Any> dispatch(event: T) {
        this.post(event) {
            for (listener in it) {
                listener(event)
            }
        }
    }

    /**
     * Dispatches an event that is cancellable.
     * When the event is cancelled it will not be posted to any listeners after.
     *
     * @param T The type of the event posted.
     * @param event the event which will be posted.
     * @return [event].
     */
    open fun <T : Cancellable> dispatch(event: T): T {
        this.post(event) {
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
     * For removing code duplication.
     *
     * @param T Type that will be posted to.
     * @param event Event to call from [registry].
     * @param block The code block to call if the list exists.
     */
    private fun <T : Any> post(event: T, block: (MutableList<Listener>) -> Unit) {
        this.registry[event::class]?.let {
            block(it)
        }

        if (this.recursive) {
            for (clazz in event::class.superclasses) {
                this.registry[clazz]?.let {
                    block(it)
                }
            }
        }
    }
}