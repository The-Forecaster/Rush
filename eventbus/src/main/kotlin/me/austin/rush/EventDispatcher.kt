package me.austin.rush

import java.util.*
import kotlin.reflect.KClass

/**
 * Basic implementation of [ReflectionBus].
 * This version is much faster than [ConcurrentEventDispatcher] but is not thread safe and can produce race conditions if running multithreaded or non-blocking projects.
 *
 * @author Austin
 * @since 2023
 *
 */
open class EventDispatcher : ReflectionBus {
    /**
     * Map that will be used to store registered [Listener] objects and their targets.
     *
     * The key-set will hold all stored [KClass] targets of [Listener] objects.
     * The value-set will hold the [MutableList] of [Listener] objects corresponding to their respective targets.
     */
    private val subscribers = mutableMapOf<KClass<*>, MutableList<Listener>>()

    /**
     * Map that is used to reduce the amount of reflection calls we have to make.
     *
     * The Key set stores an [Object] and the value set holds a [List] of [Listener] fields in that object.
     */
    private val cache = mutableMapOf<Any, List<Listener>>()

    override fun subscribe(listener: Listener) {
        val list = this.subscribers[listener.target]

        if (list == null) {
            this.subscribers[listener.target] = mutableListOf(listener)
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

    override fun subscribe(subscriber: Any) {
        for (listener in this.cache.getOrDefault(subscriber, subscriber.listenerList)) {
            this.subscribe(listener)
        }
    }

    override fun unsubscribe(listener: Listener) {
        this.subscribers[listener.target]?.let { list ->
            if (list.remove(listener) && list.isEmpty()) {
                this.subscribers.remove(listener.target)
            }
        }
    }

    override fun unsubscribe(subscriber: Any) {
        this.cache[subscriber]?.let { list ->
            for (listener in list) {
                this.unsubscribe(listener)
            }
        }
    }

    override fun <T : Any> post(event: T) {
        this.subscribers[event::class]?.let { list ->
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
    open fun <T : Cancellable> post(event: T): T {
        this.subscribers[event::class]?.let { list ->
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