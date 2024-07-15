package me.austin.rush

import kotlin.reflect.full.allSuperclasses

/**
 * Basic implementation of [ReflectionEventBus].
 * This version is much faster than [ConcurrentEventBus] but is not thread safe and can
 * produce race conditions if running multithreaded or non-blocking projects.
 *
 * @author Austin
 * @since 2023
 */
open class FastEventBus : LightEventBus(), ReflectionEventBus {
    /**
     * Map that is used to reduce the amount of reflection calls made.
     *
     * The key set stores an [Object] and the value set holds a [List] of [Listener] fields in that object.
     */
    private val cache = mutableMapOf<Any, List<Listener>>()

    override fun subscribe(subscriber: Any) {
        for (listener in this.cache.getOrPut(subscriber) { subscriber.listenerList }) {
            this.subscribe(listener)
        }
    }

    override fun unsubscribe(subscriber: Any) {
        this.cache[subscriber]?.let { list ->
            for (listener in list) { // If subscriber isn't in the cache then it hasn't been registered, so we don't need to unregister it
                this.unsubscribe(listener)
            }
        }
    }

    override fun <T : Any> postRecursive(event: T) {
        this.subscribers[event::class]?.let { list ->
            for (listener in list) {
                listener(event)
            }
        }

        for (kClass in event::class.allSuperclasses) {
            this.subscribers[kClass]?.let { list ->
                for (listener in list) {
                    listener(event)
                }
            }
        }
    }
}