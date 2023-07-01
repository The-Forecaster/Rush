package me.austin.rush

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * Basic implementation of [EventBus].
 *
 * @author Austin
 * @since 2023
 *
 */
class EventDispatcher : EventBus {
    /**
     * Map that will be used to store registered [Listener] objects and their targets.
     *
     * The key-set will hold all stored [KClass] targets of [Listener] objects.
     * The value-set will hold the [MutableList] of [Listener] objects corresponding to their respective targets.
     */
    private val registry = ConcurrentHashMap<KClass<*>, MutableList<Listener>>()

    /**
     * Map that is used to reduce the amount of reflection calls we have to make.
     *
     * The Key set stores an [Object] and the value set hold a [List] of [Listener] fields in that object.
     */
    private val cache = ConcurrentHashMap<Any, List<Listener>>()

    override fun register(listener: Listener) {
        this.registry.getOrPut(listener.target) { CopyOnWriteArrayList() }.let { list ->
            if (list.contains(listener)) {
                return
            }

            var index = 0

            while (index < list.size) {
                if (list[index].priority < listener.priority) {
                    break
                }

                index++
            }

            list.add(index, listener)
        }
    }

    override fun register(subscriber: Any) {
        for (listener in this.cache.getOrPut(subscriber) { subscriber.listenerList }) {
            this.register(listener)
        }
    }

    override fun unregister(listener: Listener) {
        this.registry[listener.target]?.let { list ->
            if (list.remove(listener) && list.isEmpty()) {
                this.registry.remove(listener.target)
            }
        }
    }

    override fun unregister(subscriber: Any) {
        this.cache[subscriber]?.let { list ->
            for (listener in list) {
                this.unregister(list)
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
}