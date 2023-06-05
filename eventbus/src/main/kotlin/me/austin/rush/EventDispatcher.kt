package me.austin.rush

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

/**
 * Basic implementation of [EventBus].
 *
 * @author Austin
 * @since 2022
 *
 * @param recursive If this eventbus posts superclasses of events posted.
 */
open class EventDispatcher(private val recursive: Boolean = false) : EventBus {
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
            this.registry[listener.target]?.let {
                it.remove(listener)

                this.registry[listener.target] = it

                if (it.size == 0) {
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
        // If subscriber isn't in the cache then it hasn't been registered, so we don't need to unregister it
        this.cache[subscriber]?.let {
            for (listener in it) {
                this.unregister(listener)
            }
        }
    }

    override fun <T : Any> dispatch(event: T) {
        this.dispatch(event) {
            for (listener in it) {
                listener(event)
            }
        }
    }

    /**
     * Dispatches an event that is cancellable.
     * When the event is cancelled it will not be posted to any listeners after.
     *
     * @param T The type of the [event] posted.
     * @param event The event which will be posted.
     * @return [event].
     */
    open fun <T : Cancellable> dispatch(event: T): T {
        this.dispatch(event) {
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
     * @param R Type that [block] will return.
     * @param event Event to call from [registry].
     * @param block The code block to call if the list exists.
     *
     * @return The result of [block]. Will be null if there is no registered listeners.
     */
    private fun <T : Any, R> dispatch(event: T, block: (MutableList<Listener>) -> R): R? {
        val out = this.registry[event::class]?.let(block)

        if (this.recursive) {
            for (clazz in event::class.allSuperclasses) {
                this.registry[clazz]?.let(block)
            }
        }

        return out
    }
}