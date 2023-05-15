package me.austin.rush

import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Annotate a listener with this class to mark it for adding to the eventbus registry
 *
 * @author Austin
 * @since 2022
 */
annotation class EventHandler

// Most of this is pasted or inspired from bush https://github.com/therealbush/eventbus-kotlin, check him out if you want to see actually good code

private val KCallable<*>.isListener: Boolean
    get() = this.hasAnnotation<EventHandler>() && this.returnType.withNullability(false)
        .isSubtypeOf(typeOf<IListener<*>>())

private val <T : Any> KClass<T>.listeners: List<KCallable<IListener<*>>>
    get() = ((this.superclasses.flatMap { it.declaredMembers } + declaredMembers)
        // This cast will never fail
        .filter(KCallable<*>::isListener) as List<KCallable<IListener<*>>>)

val Any.listeners: List<IListener<*>>
    get() = this::class.listeners.mapTo(ArrayList(this::class.listeners.size)) { it.handleCall(this) }.toList()

private fun <T> KCallable<T>.handleCall(receiver: Any? = null): T {
    val accessible = this.isAccessible
    this.isAccessible = true

    return try { call(receiver) } catch (e: Throwable) { call() } finally { this.isAccessible = accessible }
}