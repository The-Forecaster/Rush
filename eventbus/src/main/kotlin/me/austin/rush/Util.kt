package me.austin.rush

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Annotate a listener with this class to mark it for adding to the eventbus registry.
 *
 * @author Austin
 * @since 2022
 */
annotation class EventHandler

/**
 * Default [CoroutineScope] which be called when an [AsyncListener] is called.
 */
val defaultScope = CoroutineScope(Dispatchers.Default)

// Check out https://github.com/therealbush/eventbus-kotlin if you want to see where this logic comes from

/**
 * Using [KClass.members] only returns public members, and using [KClass.declaredMembers]
 * doesn't return inherited members. This returns all members, private and inherited.
 *
 * @return All members, private and inherited.
 */
internal val KClass<*>.allMembers: Sequence<KCallable<*>>
    get() {
        return (this.declaredMembers + this.allSuperclasses.flatMap { it.declaredMembers }).asSequence()
    }

/**
 * Unwraps the object referenced in a [KCallable].
 *
 * @param R Type of the [KCallable]
 * @param receiver Object containing the [KCallable] if it is non-static.
 *
 * @return The object referenced by the [KCallable].
 */
private fun <R> KCallable<R>.handleCall(receiver: Any? = null): R {
    val accessible = this.isAccessible
    this.isAccessible = true
    // Doing this so we don't leak accessibility
    return try { call(receiver) } catch (e: Throwable) { call() } finally { this.isAccessible = accessible }
}

/**
 * Finds all [Listener] objects inside this object.
 *
 * @return [List] of [Listener] objects inside this object.
 */
val Any.listeners: List<Listener>
    get() {
        return this::class.allMembers.filter {
            it.hasAnnotation<EventHandler>() && it.returnType.withNullability(false)
                .isSubtypeOf(typeOf<Listener>()) && it.valueParameters.isEmpty()
        }.map { it.handleCall(this) as Listener }.toList()
    }