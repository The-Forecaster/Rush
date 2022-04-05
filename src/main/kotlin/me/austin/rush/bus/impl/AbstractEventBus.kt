package me.austin.rush.bus.impl

import me.austin.rush.bus.EventBus
import me.austin.rush.listener.Listener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

abstract class AbstractEventBus(
    protected val type: Class<*>
) : EventBus {
    override val registry: ConcurrentHashMap<Class<*>, MutableSet<Listener<*>>> = ConcurrentHashMap()

    private val subscribers: MutableSet<Any> = Collections.synchronizedSet(mutableSetOf())

    /**
     * Finds and registers all valid listener fields in a target object class. Will then sort them
     * after adding them.
     */
    abstract fun registerFields(subscriber: Any)

    /** Finds and removes all valid fields from the subscriber registry */
    abstract fun unregisterFields(subscriber: Any)

    override fun register(subscriber: Any) {
        if (isRegistered(subscriber)) return

        this.registerFields(subscriber)

        this.subscribers.add(subscriber)
    }

    override fun unregister(subscriber: Any) {
        if (!isRegistered(subscriber)) return

        this.unregisterFields(subscriber)

        this.subscribers.remove(subscriber)
    }

    override fun isRegistered(subscriber: Any): Boolean = this.subscribers.contains(subscriber)

    override fun <T> dispatch(event: T): T {
        if (this.registry[event!!::class.java]?.size != 0) {
            this.getOrPutList(event.javaClass).stream().forEach { listener ->
                listener(event)
            }
        }

        return event
    }

    private fun <T : Any> getOrPutList(clazz: Class<T>): CopyOnWriteArraySet<Listener<T>> = this.registry.getOrPut(clazz, ::CopyOnWriteArraySet) as CopyOnWriteArraySet<Listener<T>>
}
