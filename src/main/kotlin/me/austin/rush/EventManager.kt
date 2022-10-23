package me.austin.rush

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Basic implementation of [EventBus]
 */
open class EventManager : EventBus {
    override val registry = ConcurrentHashMap<KClass<*>, MutableList<Listener<*>>>()

    private val cache = ConcurrentHashMap<Any, MutableList<Listener<*>>>()

    override fun register(listener: Listener<*>) {
        this.registry.getOrPut(listener.target, ::CopyOnWriteArrayList).let {
            if (it.contains(listener)) return

            var index = 0

            while (index < it.size) {
                if (it[index].priority < listener.priority) break

                index++
            }

            it.add(index, listener)
        }
    }

    override fun unregister(listener: Listener<*>) {
        this.registry[listener.target]?.remove(listener)
    }

    override fun register(subscriber: Any) {
        for (listener in this.cache.getOrPut(subscriber, subscriber::listeners)) this.register(listener)
    }

    override fun unregister(subscriber: Any) {
        for (listener in subscriber.listeners) this.unregister(listener)
    }

    override fun <T : Any> dispatch(event: T) {
        (registry[event::class] as? MutableList<Listener<T>>)?.let { synchronized(it) { it.forEach { it(event) } } }
    }

    fun <T : Cancellable> dispatch(event: T): T {
        (registry[event::class] as? MutableList<Listener<T>>)?.let {
            synchronized(it) {
                it.forEach {
                    if (event.isCancelled) return@forEach
                    it(event)
                }
            }
        }

        return event
    }
}

// Most of this is pasted from bush https://github.com/therealbush/eventbus-kotlin, check him out if you want to see actually good code

private val KCallable<*>.isListener
    get() = this.findAnnotation<EventHandler>() != null && this.returnType.isSubtypeOf(typeOf<Listener<*>>())

private val <T : Any> KClass<T>.listeners
    get() = this.declaredMembers.filter(KCallable<*>::isListener) as List<KCallable<Listener<*>>>

private val Any.listeners
    get() = this::class.listeners.map { it.handleCall(this) }.toMutableList()

private fun <T : Any> KCallable<T>.handleCall(receiver: Any? = null): T {
    val accessible = this.isAccessible
    this.isAccessible = true

    // This will get a both static and non-static listeners in the jvm
    return try { call(receiver) } catch(e: Throwable) { call() } finally { this.isAccessible = accessible }
}
