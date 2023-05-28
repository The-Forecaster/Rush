package me.austin.rush

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Annotate a listener with this class to mark it for adding to the eventbus registry
 *
 * @author Austin
 * @since 2022
 */
annotation class EventHandler

/**
 * Default [CoroutineScope] which be called when an [AsyncListener] is called
 */
val defaultScope = CoroutineScope(Dispatchers.Default)

// Most of this is inspired from bush https://github.com/therealbush/eventbus-kotlin, check him out if you want to see actually good code

/**
 * Returns if this [KCallable] is a valid listener
 */
private val KCallable<*>.isListener: Boolean
    get() {
        return this.hasAnnotation<EventHandler>() && this.returnType.isSubtypeOf(typeOf<Listener>())
    }

/**
 * Returns a [List] of KCallable of listeners
 */
private val KClass<*>.listeners: List<KCallable<Listener>>
    get() {
        val out = mutableListOf<KCallable<Listener>>()

        for (callable in declaredMembers + this.superclasses.flatMap { it.declaredMembers }) {
            if (callable.isListener) {
                out.add(callable as KCallable<Listener>)
            }
        }

        return out.toList()
    }

/**
 * Returns a [List] of listeners inside of this object
 */
internal val Any.listeners: List<Listener>
    get() {
        return this::class.listeners.mapTo(ArrayList(this::class.listeners.size)) {
            it.handleCall(this)
        }
    }

/**
 * Unwraps an object contained in a [KCallable]
 *
 * @param T Type of the [KCallable]
 * @param receiver Object containing the [KCallable] if it is non-static
 *
 * @return The object inside the [KCallable]
 */
private fun <T> KCallable<T>.handleCall(receiver: Any? = null): T {
    val accessible = this.isAccessible
    this.isAccessible = true

    return try { call(receiver) } catch (e: Throwable) { call() } finally { this.isAccessible = accessible }
}