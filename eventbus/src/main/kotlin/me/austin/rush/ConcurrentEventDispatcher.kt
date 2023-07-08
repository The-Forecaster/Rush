package me.austin.rush

import java.util.*
import kotlin.reflect.KClass

/**
 * Thread-safe implementation of [EventBus].
 * This version is slower than [EventDispatcher] but is thread safe for multithreaded or non-blocking projects.
 *
 * @author Austin
 * @since 2022
 */
open class ConcurrentEventDispatcher : EventBus {
    /**
     * Map that will be used to store registered [Listener] objects and their targets.
     *
     * The key-set will hold all stored [KClass] targets of [Listener] objects.
     * The value-set will hold the [Array] of [Listener] objects corresponding to their respective targets.
     */
    private val registry = mutableMapOf<KClass<*>, Array<Listener>>()

    /**
     * Map that is used to reduce the amount of reflection calls we have to make.
     *
     * The Key set stores an [Object] and the value set hold an [Array] of [Listener] fields in that object.
     */
    private val cache = mutableMapOf<Any, Array<Listener>>()

    /**
     * This is so we only ever have 1 write action going on at a time.
     */
    // Need to make sure this won't double lock, more tests coming in the future
    private val writeSync = Any()

    override fun register(listener: Listener) {
        synchronized(writeSync) {
            val array = this.registry[listener.target]

            if (array == null) {
                this.registry[listener.target] = arrayOf(listener)
            } else {
                if (listener in array) {
                    return
                }

                val index = Arrays.binarySearch(array, listener).let { i ->
                    if (i < 0) {
                        -i - 1
                    } else {
                        i
                    }
                }

                val newArray = arrayOfNulls<Listener>(array.size + 1)

                System.arraycopy(array, 0, newArray, 0, index)
                newArray[index] = listener
                System.arraycopy(array, index, newArray, index + 1, array.size - index)

                @Suppress("UNCHECKED_CAST")
                this.registry[listener.target] = newArray as Array<Listener>
                return
            }
        }
    }

    override fun unregister(listener: Listener) {
        synchronized(writeSync) {
            val array = this.registry[listener.target]

            if (array != null) {
                val index = array.indexOf(listener)

                if (index < 0) {
                    return
                }

                if (array.size == 1) {
                    this.registry.remove(listener.target)
                } else {
                    val newArray = arrayOfNulls<Listener>(array.size - 1)

                    // Copy around the listener
                    System.arraycopy(array, 0, newArray, 0, index)
                    System.arraycopy(array, index + 1, newArray, index, array.size - index - 1)

                    @Suppress("UNCHECKED_CAST")
                    this.registry[listener.target] = newArray as Array<Listener>
                }
            }
        }
    }

    override fun register(subscriber: Any) {
        for (listener in this.cache.getOrDefault(subscriber, subscriber.listenerArray)) {
            this.register(listener)
        }
    }

    override fun unregister(subscriber: Any) {
        // If subscriber isn't in the cache then it hasn't been registered, so we don't need to unregister it
        this.cache[subscriber]?.let { array ->
            for (listener in array) {
                this.unregister(listener)
            }
        }
    }

    override fun <T : Any> dispatch(event: T) {
        this.registry[event::class]?.let { array ->
            for (listener in array) {
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
        this.registry[event::class]?.let { array ->
            for (listener in array) {
                listener(event)

                if (event.isCancelled) {
                    break
                }
            }
        }

        return event
    }
}