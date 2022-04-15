package trans.rights.event.bus.impl

import me.austin.rush.annotation.EventHandler
import trans.rights.event.bus.EventBus
import trans.rights.event.listener.Listener
import trans.rights.event.listener.impl.LambdaListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberProperties

object BasicEventManager : EventManager(LambdaListener::class)

open class EventManager(private val type: KClass<out Listener<*>>) : EventBus {
    constructor(type: Class<out Listener<*>>) : this(type.kotlin)

    override val registry: MutableMap<KClass<*>, MutableSet<Listener<*>>> = ConcurrentHashMap()

    private val subscribers: MutableSet<Any> = CopyOnWriteArraySet()

    override fun register(listener: Listener<*>) {
        this.registry.getOrPut(listener.target, ::CopyOnWriteArraySet).let {
            it.add(listener)
            this.registry[listener.target] = CopyOnWriteArraySet(it.sorted())
        }
    }

    override fun unregister(listener: Listener<*>) {
        this.registry[listener.target]?.remove(listener)
    }

    override fun register(subscriber: Any) {
        if (isRegistered(subscriber)) return

        this.filter(subscriber::class.memberProperties).forEach(this::register)

        this.subscribers.add(subscriber)
    }

    override fun unregister(subscriber: Any) {
        if (!isRegistered(subscriber)) return

        this.filter(subscriber::class.memberProperties).forEach(this::unregister)

        this.subscribers.remove(subscriber)
    }

    override fun isRegistered(subscriber: Any): Boolean = this.subscribers.contains(subscriber)

    override fun <T> dispatch(event: T): T {
        if (this.registry[event!!::class]?.size != 0) {
            this.getList(event.javaClass)?.stream()?.forEach { listener ->
                listener(event)
            }
        }

        return event
    }

    private fun filter(list: Collection<KProperty<*>>): Stream<out Listener<*>> {
        return list.stream().filter(this::isValid) as Stream<out Listener<*>>
    }

    private fun <T : Any> getList(clazz: Class<T>): CopyOnWriteArraySet<out Listener<T>>? {
        return this.registry[clazz.kotlin] as CopyOnWriteArraySet<out Listener<T>>?
    }

    private fun isValid(property: KProperty<*>): Boolean {
        return property.annotations.contains(EventHandler()) && type.isSuperclassOf(property::class)
    }
}
