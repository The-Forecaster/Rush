package me.austin.rush

import java.util.*
import kotlin.reflect.KClass

/**
 * Basic implementation of [EventBus].
 * This version is much faster than [ConcurrentEventDispatcher] but is not thread safe and can produce race conditions if running multithreaded or non-blocking projects.
 *
 * @author Austin
 * @since 2023
 *
 */
open class EventDispatcher : EventBus {
    /**
     * Map that will be used to store registered [Listener] objects and their targets.
     *
     * The key-set will hold all stored [KClass] targets of [Listener] objects.
     * The value-set will hold the [MutableList] of [Listener] objects corresponding to their respective targets.
     */
    private val registry = mutableMapOf<KClass<*>, MutableList<Listener>>()

    /**
     * Map that is used to reduce the amount of reflection calls we have to make.
     *
     * The Key set stores an [Object] and the value set holds a [List] of [Listener] fields in that object.
     */
    private val cache = mutableMapOf<Any, List<Listener>>()

    override fun register(listener: Listener) {
        val list = this.registry[listener.target]

        if (list == null) {
            this.registry[listener.target] = mutableListOf(listener)
        } else {
            if (listener in list) {
                return
            }

            val index = Collections.binarySearch(list, listener).let { i ->
                if (i < 0) {
                    -i - 1
                } else {
                    i
                }
            }

            list.add(index, listener)
        }
    }

    override fun register(subscriber: Any) {
        for (listener in this.cache.getOrDefault(subscriber, subscriber.listenerList)) {
            this.register(listener)
        }
    }

    override fun unregister(listener: Listener) {
        this.registry[listener.target]?.let { list ->
            if (list.remove(listener)) {
                if (list.isEmpty()) {
                    this.registry.remove(listener.target)
                }
            }
        }
    }

    override fun unregister(subscriber: Any) {
        this.cache[subscriber]?.let { list ->
            for (listener in list) {
                this.unregister(listener)
            }
        }
    }

    override fun <T : Any> dispatch(event: T) {
        this.registry[event::class]?.let { list ->
            for (listener in list) {
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
        this.registry[event::class]?.let { list ->
            for (listener in list) {
                listener(event)

                if (event.isCancelled) {
                    break
                }
            }
        }

        return event
    }
}