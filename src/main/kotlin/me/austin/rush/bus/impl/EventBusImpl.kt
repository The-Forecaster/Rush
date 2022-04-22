package me.austin.rush.bus.impl

import me.austin.rush.type.EventHandler
import me.austin.rush.bus.EventBus
import me.austin.rush.listener.Listener
import me.austin.rush.listener.impl.LambdaListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSuperclassOf

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

        this.filter(subscriber::class.declaredMemberProperties).forEach(this::register)

        this.subscribers.add(subscriber)
    }

    override fun unregister(subscriber: Any) {
        if (!isRegistered(subscriber)) return

        this.filter(subscriber::class.declaredMemberProperties).forEach(this::unregister)

        this.subscribers.remove(subscriber)
    }

    override fun isRegistered(subscriber: Any): Boolean = this.subscribers.contains(subscriber)

    override fun <T : Any> dispatch(event: T): T {
        if ((this.registry[event::class]?.size ?: 0) != 0) {
            this.getList(event.javaClass)?.stream()?.forEach { listener ->
                listener(event)
            }
        }

        return event
    }

    private fun filter(list: Collection<KProperty<*>>): Stream<out Listener<*>> = list.stream().filter(this::isValid) as Stream<out Listener<*>>

    private fun <T : Any> getList(clazz: Class<T>): CopyOnWriteArraySet<out Listener<T>>? = this.registry[clazz.kotlin] as CopyOnWriteArraySet<out Listener<T>>?

    private fun isValid(property: KProperty<*>): Boolean = property.annotations.contains(EventHandler()) && type.isSuperclassOf(property::class)
}
