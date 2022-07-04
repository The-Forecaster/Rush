package me.austin.rush.bus.impl

import me.austin.rush.bus.EventBus
import me.austin.rush.listener.Listener
import me.austin.rush.listener.impl.EventHandler
import me.austin.rush.listener.impl.LambdaListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.stream.Collectors
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.superclasses

open class EventManager(private val type: KClass<out Listener<*>> = LambdaListener::class) : EventBus {
    constructor(type: Class<out Listener<*>>) : this(type.kotlin)

    override val registry: MutableMap<KClass<*>, MutableList<Listener<*>>> = ConcurrentHashMap()

    private val cache: MutableMap<Any, MutableList<Listener<*>>> = ConcurrentHashMap()

    override fun register(listener: Listener<*>) = this.registry.getOrPut(listener.target, ::CopyOnWriteArrayList).let {
        var index = 0

        while (index < it.size) {
            if (it[index].priority < listener.priority) break

            index++
        }

        it.add(index, listener)
    }

    override fun unregister(listener: Listener<*>) {
        this.registry[listener.target]?.remove(listener)
    }

    override fun register(subscriber: Any): Unit = this.cache.getOrPut(subscriber) {
        subscriber::class.declaredMemberProperties.stream().filter(this::isValid).map(this::asListener).collect(
            Collectors.toList()
        )
    }.forEach(this::register)

    override fun unregister(subscriber: Any): Unit = subscriber::class.declaredMemberProperties.stream().filter(this::isValid).map(this::asListener).forEach(this::unregister)

    override fun <T : Any> dispatch(event: T): T {
        (registry[event::class] as MutableList<Listener<T>>?)?.forEach { it(event) }

        return event
    }

    private fun asListener(property: KProperty<*>) = property as Listener<*>

    private fun isValid(property: KProperty<*>) = property.annotations.contains(EventHandler()) && property::class.superclasses.contains(Listener::class)
}
