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
    // Need to make sure this won't double lock, more tests coming in the future
    private val writeSync = Any()

    override fun subscribe(listener: Listener) {
        synchronized(writeSync) {
            val array = this.subscribers[listener.target]

            if (array == null) {
                this.subscribers[listener.target] = arrayOf(listener)
            } else {
                if (listener in array) {
                    return
                }

                val index = array.binarySearch(listener).let { i ->
                    // This slows this down considerably, but is needed or else binarySearch will provide an incorrect index
                    if (i < 0) {
                        -i - 1
                    } else {
                        i
                    }
                }

                val newArray = arrayOfNulls<Listener>(array.size + 1)

                // Copy on write action (thanks for the code Brady)
                System.arraycopy(array, 0, newArray, 0, index)
                newArray[index] = listener
                System.arraycopy(array, index, newArray, index + 1, array.size - index)

                @Suppress("UNCHECKED_CAST")
                this.subscribers[listener.target] = newArray as Array<Listener>
            }
        }
    }

    override fun unsubscribe(listener: Listener) {
        synchronized(writeSync) {
            this.subscribers[listener.target]?.let { array ->
                val index = array.indexOf(listener)

                // If listener isn't found then index will be -1
                if (index < 0) {
                    return
                }

                if (array.size == 1) {
                    // This is slow but will improve posting performance with fewer keys to iterate through
                    this.subscribers.remove(listener.target)
                } else {
                    val newArray = arrayOfNulls<Listener>(array.size - 1)

                    // Copy around the listener
                    System.arraycopy(array, 0, newArray, 0, index)
                    System.arraycopy(array, index + 1, newArray, index, array.size - index - 1)

                    @Suppress("UNCHECKED_CAST")
                    this.subscribers[listener.target] = newArray as Array<Listener>
                }
            }

            val array = this.subscribers[listener.target]

            if (array != null) {
                val index = array.indexOf(listener)

                if (index < 0) {
                    return
                }

                if (array.size == 1) {
                    this.subscribers.remove(listener.target)
                } else {
                    val newArray = arrayOfNulls<Listener>(array.size - 1)

                    // Copy around the listener
                    System.arraycopy(array, 0, newArray, 0, index)
                    System.arraycopy(array, index + 1, newArray, index, array.size - index - 1)

                    @Suppress("UNCHECKED_CAST")
                    this.subscribers[listener.target] = newArray as Array<Listener>
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
        // If subscriber isn't in the cache then it hasn't been registered, so we don't need to unregister it
        this.cache[subscriber]?.let { array ->
            for (listener in array) {
                this.unsubscribe(listener)
            }
        }
    }

    override fun <T : Any> post(event: T) {
        this.subscribers[event::class]?.let { array ->
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
    open fun <T : Cancellable> post(event: T): T {
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