package me.austin.rush

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * Default implementation of [EventBus]. This bus does not provide reflection, so you will have to subscribe each [Listener]
 * individually instead of being able to scan for [Listener] fields within a class.
 *
 * @author Austin
 * @since 2023
 */
open class LightEventBus : EventBus {
    /**
     * Map that will be used to store registered [Listener] objects and their targets.
     *
     * The key-set will hold all stored [KClass] targets of [Listener] objects.
     * The value-set will hold the [MutableList] of [Listener] objects corresponding to their respective targets.
     */
    protected val subscribers = mutableMapOf<KClass<*>, MutableList<Listener>>()

    override fun subscribe(listener: Listener) {
        val list = this.subscribers[listener.target]

        if (list == null) { // ?.let because it will capture subscribers in a closure, this will avoid that
            this.subscribers[listener.target] = CopyOnWriteArrayList(arrayOf(listener))
        } else {
            if (listener in list) {
                return
            }

            list.add(list.binarySearch(listener).let { i ->
                if (i < 0) {
                    -i - 1
                } else {
                    i
                }
            }, listener)
        }
    }

    override fun unsubscribe(listener: Listener) {
        this.subscribers[listener.target]?.let { list ->
            list.remove(listener)

            if (list.isEmpty()) {
                this.subscribers.remove(listener.target)
            }
        }
    }

    override fun <T : Any> post(event: T): T {
        this.subscribers[event::class]?.let { list ->
            for (listener in list) {
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
}