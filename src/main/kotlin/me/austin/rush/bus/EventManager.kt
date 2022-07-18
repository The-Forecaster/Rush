package me.austin.rush.bus

import me.austin.rush.listener.EventHandler
import me.austin.rush.listener.Listener
import me.austin.rush.type.Cancellable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.*
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible

/**
 * Basic implementation of [EventBus]
 */
open class EventManager : EventBus {

    override val registry = ConcurrentHashMap<KClass<*>, MutableList<Listener<*>>>()

    private val cache = ConcurrentHashMap<Any, MutableList<Listener<*>>>()

    override fun register(listener: Listener<*>) = this.registry.getOrPut(listener.target, ::CopyOnWriteArrayList).let {
        if (it.contains(listener)) return@let false

        var index = 0

        while (index < it.size) {
            if (it[index].priority < listener.priority) break

            index++
        }

        it.add(index, listener)
        return@let true
    }

    override fun unregister(listener: Listener<*>) = this.registry[listener.target]?.remove(listener) ?: throw RuntimeException("This listener was never registered")

    override fun register(subscriber: Any): Boolean = this.cache.getOrPut(subscriber, subscriber::listeners).map(::register).all()

    override fun unregister(subscriber: Any): Boolean = subscriber.listeners.map(::unregister).all()

    override fun <T : Any> dispatch(event: T): T {
        (registry[event::class] as MutableList<Listener<T>>?)?.forEach { it(event) }
        return event
    }

    fun <T : Cancellable> dispatch(event: T): T {
        (registry[event::class] as MutableList<Listener<T>>?)?.forEach {
            it(event)
            if (event.isCancelled) return@forEach
        }
        return event
    }
}

// Most of this is pasted from bush https://github.com/therealbush/eventbus-kotlin, check him out if you want to see actually good code

private inline val KCallable<*>.isListener
    get() = this.findAnnotation<EventHandler>() != null && this.returnType.isSubtypeOf(typeOf<Listener<*>>())

private inline val <T : Any> KClass<T>.listeners
    get() = this.declaredMembers.filter(KCallable<*>::isListener) as List<KCallable<Listener<*>>>

private inline val Any.listeners
    get() = this::class.listeners.map { it.handleCall(this) }.toMutableList()

private inline fun <reified T : Any> KCallable<T>.handleCall(receiver: Any? = null): T {
    this.isAccessible = true
    return runCatching { call(receiver) }.getOrElse { call() }
}
