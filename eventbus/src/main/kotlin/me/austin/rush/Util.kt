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

// Check out https://github.com/therealbush/eventbus-kotlin if you want to see where this logic comes from

/**
 * Returns a [List] of listeners inside of this object
 */
internal val Any.listeners: List<Listener>
    get() {
        val klass = this::class

        val out = mutableListOf<Listener>()

        for (callable in klass.declaredMembers + klass.superclasses.flatMap { it.declaredMembers }) {
            if (callable.hasAnnotation<EventHandler>() && callable.returnType.isSubtypeOf(typeOf<Listener>())) {
                out.add(callable.handleCall(this) as Listener)
            }
        }

        return out
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