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
@Target(AnnotationTarget.FIELD)
annotation class EventHandler

/**
 * Default [CoroutineScope] which be called when an [AsyncListener] is called.
 */
val defaultScope = CoroutineScope(Dispatchers.Default)

// Check out https://github.com/therealbush/eventbus-kotlin if you want to see where this logic comes from

/**
 * Using [KClass.members] only returns public members, and using [KClass.declaredMembers]
 * doesn't return inherited members. This function, while slower than those two, is able
 * to retrieve all [KCallable] members inside a class.
 *
 * @return A [Sequence] of all members, private and inherited.
 */
internal val KClass<*>.allMembers: Sequence<KCallable<*>>
    get() {
        // asSequence just creates a wrapper for better filter and map actions, so it's better to do it this way
        return (this.declaredMembers + this.allSuperclasses.flatMap { it.declaredMembers }).asSequence()
    }

/**
 * Unwraps the object referenced in a [KCallable].
 *
 * @param R Type parameter of the [KCallable]
 * @param receiver Object containing the [KCallable] if it is non-static.
 *
 * @return The field referenced by the [KCallable].
 */
private fun <R> KCallable<R>.handleCall(receiver: Any?): R {
    val accessible = this.isAccessible
    this.isAccessible = true
    // Doing this so we don't leak accessibility
    return try { call(receiver) } catch (e: Throwable) { call() } finally { this.isAccessible = accessible }
}

/**
 * Returns all [KCallable] fields in this class that reference [Listener] fields.
 *
 * @return A [Sequence] of [KCallable] objects that reference [Listener] fields.
 */
private val KClass<*>.listeners: Sequence<KCallable<Listener>>
    get() {
        return this.allMembers.filter {
            it.hasAnnotation<EventHandler>() && it.returnType.withNullability(false)
                .isSubtypeOf(typeOf<Listener>()) && it.valueParameters.isEmpty()
        } as Sequence<KCallable<Listener>>
    }

/**
 * Finds all [Listener] fields inside this object's class.
 *
 * @return An [Array] of [Listener] fields inside this object.
 */
val Any.listeners: List<Listener>
    get() {
        return this::class.listeners.map { it.handleCall(this) }.toList()
    }