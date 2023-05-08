package me.austin.light

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

open class EventManager {
    val registry = ConcurrentHashMap<KClass<*>, MutableList<(Any) -> Unit>>()

    inline fun <reified T : Any> register(noinline action: (T) -> Unit) =
        this.registry.getOrPut(T::class) { CopyOnWriteArrayList() }.add(action as (Any) -> Unit)

    inline fun <reified T : Any> unregister(noinline action: (T) -> Unit) = this.registry[T::class]?.remove(action)

    fun post(event: Any) {
        this.registry[event::class]?.let {
            synchronized(it) { for (action in it) action(event) }
        }
    }
}