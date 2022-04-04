package trans.rights.event.bus

import trans.rights.event.bus.ListenerType.*
import trans.rights.event.listener.Listener
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

abstract class AbstractEventBus(
    private val type: ListenerType,
    private val subscribers: MutableSet<Any> = Collections.synchronizedSet(mutableSetOf()),
    protected val registry: ConcurrentHashMap<Class<*>, MutableSet<Listener<*>>> = ConcurrentHashMap()
) : EventBus {
    constructor() : this(BOTH)

    /**
     * Finds and registers all valid listener fields in a target object class. Will then sort them
     * after adding them.
     */
    abstract fun registerFields(subscriber: Any)

    /**
     * Finds and registers all valid methods in a target object class. Will then sort them after
     * adding them.
     */
    abstract fun registerMethods(subscriber: Any)

    /** Finds and removes all valid fields from the subscriber registry */
    abstract fun unregisterFields(subscriber: Any)

    /** Finds and removes all valid methods from the subscriber registry */
    abstract fun unregisterMethods(subscriber: Any)

    override fun register(subscriber: Any) {
        if (isRegistered(subscriber)) return

        when (this.type) {
            LAMBDA -> this.registerFields(subscriber)
            METHOD -> this.registerMethods(subscriber)
            else -> {
                this.registerFields(subscriber)
                this.registerMethods(subscriber)
            }
        }

        this.subscribers.add(subscriber)
    }

    override fun unregister(subscriber: Any) {
        if (!isRegistered(subscriber)) return

        when (this.type) {
            LAMBDA -> this.unregisterFields(subscriber)
            METHOD -> this.unregisterMethods(subscriber)
            else -> {
                this.registerFields(subscriber)
                this.registerMethods(subscriber)
            }
        }

        this.subscribers.remove(subscriber)
    }

    override fun isRegistered(subscriber: Any): Boolean {
        return this.subscribers.contains(subscriber)
    }

    override fun <T : Any> dispatch(event: T): T {
        if (this.registry[event::class.java]?.size != 0) {
            this.getOrPutList(event.javaClass).stream().forEach { listener ->
                listener(event)
            }
        }

        return event
    }

    protected fun <T : Any> getOrPutList(clazz: Class<T>): CopyOnWriteArraySet<Listener<T>> {
        return this.registry.getOrPut(clazz, ::CopyOnWriteArraySet) as CopyOnWriteArraySet<Listener<T>>
    }
}

enum class ListenerType {
    METHOD,
    LAMBDA,
    BOTH
}
