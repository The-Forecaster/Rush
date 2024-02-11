package me.austin.rush

import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

/**
 * Thread-safe implementation of [ReflectionEventBus].
 * This version is slower than [FastEventBus] but is thread safe for multithreaded or non-blocking projects.
 *
 * @author Austin
 * @since 2022
 */
open class ConcurrentEventBus : ReflectionEventBus {
    /**
     * Map that will be used to store registered [Listener] objects and their targets.
     *
     * The key-set will hold all stored [KClass] targets of [Listener] objects.
     * The value-set will hold the [Array] of [Listener] objects corresponding to their respective targets.
     */
    private val subscribers = mutableMapOf<KClass<*>, Array<Listener>>()

    /**
     * Map that is used to reduce the amount of reflection calls we have to make.
     *
     * The Key set stores an [Object] and the value set hold an [Array] of [Listener] fields in that object.
     */
    private val cache = mutableMapOf<Any, Array<Listener>>()

    /**
     * This is so we only ever have 1 write action going on at a time.
     */
    private val writeSync = Any() // Need to make sure this won't double lock, more tests coming in the future

    override fun subscribe(listener: Listener) {
        synchronized(writeSync) {
            val array = this.subscribers[listener.target]

            if (array == null) { // ?.let because it will capture subscribers in a closure, this will avoid that
                this.subscribers[listener.target] = arrayOf(listener)
            } else {
                if (listener in array) {
                    return
                }

                val index = array.binarySearch(listener).let { i -> // This will return negative values sometimes
                    if (i < 0) {
                        -i - 1 // Invert the value to prevent crashes
                    } else {
                        i
                    }
                }

                val dst = arrayOfNulls<Listener>(array.size + 1)

                // Copy on write action (thanks for the code Brady)
                System.arraycopy(array, 0, dst, 0, index)
                dst[index] = listener
                System.arraycopy(array, index, dst, index + 1, array.size - index)

                @Suppress("UNCHECKED_CAST") // Should never error
                this.subscribers[listener.target] = dst as Array<Listener>
            }
        }
    }

    override fun unsubscribe(listener: Listener) {
        synchronized(writeSync) {
            val array = this.subscribers[listener.target]

            if (array != null) { // ?.let because it will capture subscribers in a closure, this will avoid that
                val index = array.indexOf(listener)

                if (index < 0) {
                    return
                }

                if (array.size == 1) {
                    this.subscribers.remove(listener.target)
                } else {
                    val dst = arrayOfNulls<Listener>(array.size - 1)

                    System.arraycopy(array, 0, dst, 0, index) // Copy up to the listener
                    System.arraycopy(array, index + 1, dst, index, array.size - index - 1) // Copy after

                    @Suppress("UNCHECKED_CAST") // Should never error
                    this.subscribers[listener.target] = dst as Array<Listener>
                }
            }
        }
    }

    override fun subscribe(subscriber: Any) {
        for (listener in this.cache.getOrPut(subscriber) { subscriber.listenerArray }) {
            this.subscribe(listener)
        }
    }

    override fun unsubscribe(subscriber: Any) {
        this.cache[subscriber]?.let { array -> // If subscriber isn't in the cache then it hasn't been registered, so we don't need to unregister it
            for (listener in array) {
                this.unsubscribe(listener)
            }
        }
    }

    override fun <T : Any> post(event: T): T {
        this.subscribers[event::class]?.let { array ->
            for (listener in array) {
                listener(event)
            }
        }

        return event
    }

    override fun <T : Cancellable> post(event: T): T {
        this.subscribers[event::class]?.let { array ->
            for (listener in array) {
                listener(event)

                if (event.isCancelled) {
                    break
                }
            }
        }

        return event
    }

    override fun <T : Any> postRecursive(event: T) {
        this.subscribers[event::class]?.let { array ->
            for (listener in array) {
                listener(event)
            }
        }

        for (kClass in event::class.allSuperclasses) {
            this.subscribers[kClass]?.let { array ->
                for (listener in array) {
                    listener(event)
                }
            }
        }
    }
}