package me.austin.rush.bus.impl

import me.austin.rush.bus.EventBus
import me.austin.rush.listener.Listener
import me.austin.rush.annotation.EventHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import java.util.Arrays
import java.util.stream.Stream
import java.lang.reflect.Field
import kotlin.reflect.KClass

open class EventManager(private val type: Class<out Listener<*>>) : EventBus {
    override val registry: MutableMap<KClass<*>, MutableSet<Listener<*>>> = ConcurrentHashMap()

    private val subscribers: MutableSet<Any> = CopyOnWriteArraySet()

    override fun register(listener: Listener<*>) {
        this.registry.getOrPut(listener.target, ::CopyOnWriteArraySet).let {
            it.add(listener)
            it.toSortedSet()
        }
    }

    override fun unregister(listener: Listener<*>) {
        this.registry[listener.target]?.remove(listener)
    }

    override fun register(subscriber: Any) {
        if (isRegistered(subscriber)) return

        this.filter(subscriber::class.java.fields).forEach(this::register)

        this.subscribers.add(subscriber)
    }

    override fun unregister(subscriber: Any) {
        if (!isRegistered(subscriber)) return

        this.filter(subscriber::class.java.fields).forEach(this::unregister)

        this.subscribers.remove(subscriber)
    }

    override fun isRegistered(subscriber: Any): Boolean = this.subscribers.contains(subscriber)

    override fun <T> dispatch(event: T): T {
        if (this.registry[event!!::class]?.size != 0) {
            this.getList(event.javaClass).stream().forEach { listener ->
                listener(event)
            }
        }

        return event
    }

    private fun filter(list: Array<out Field>): Stream<Listener<*>> {
        return Arrays.stream(list).filter(this::isValid) as Stream<Listener<*>>
    }

    private fun <T : Any> getList(clazz: Class<T>): CopyOnWriteArraySet<Listener<T>> {
        return this.registry[clazz.kotlin] as CopyOnWriteArraySet<Listener<T>>
    }

    private fun isValid(field: Field): Boolean {
        return field.isAnnotationPresent(EventHandler::class.java) && this.type.isAssignableFrom(field.type)
    }
}
