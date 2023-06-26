package me.austin.rush

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * Annotate a [Listener] with this class to mark it for adding to the [EventBus].
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
 * doesn't return inherited members. This function, while slower than those two, is able
 * to retrieve all [KCallable] members inside a class.
 *
 * @return A [List] of all members, private and inherited.
 */
private val KClass<*>.allMembers: Sequence<KCallable<*>>
    get() {
        // asSequence just creates a wrapper for better filter and map actions, so it's better to do it this way
        return (this.declaredMembers + this.allSuperclasses.flatMap { kClass -> kClass.declaredMembers }).asSequence()
    }

/**
 * Unwraps the object referenced in a [KCallable].
 *
 * @param R Type parameter of the [KCallable]
 * @param receiver Object containing the [KCallable] if it is non-static.
 *
 * @return The [R] referenced by the [KCallable].
 */
private fun <R> KCallable<R>.handleCall(receiver: Any): R {
    val accessible = this.isAccessible
    this.isAccessible = true
    // Doing this so we don't leak accessibility
    return try { this.call(receiver) } catch (e: Throwable) { this.call() } finally { this.isAccessible = accessible }
}

/**
 * Returns all [KCallable] fields in this class that reference [Listener] fields.
 *
 * @return A [List] of [KCallable] objects that reference [Listener] fields.
 */
private inline val KClass<*>.listeners: Sequence<KCallable<Listener>>
    get() {
        return this.allMembers.filter { kCallable ->
            kCallable.returnType.isSubtypeOf(typeOf<Listener>())
        } as Sequence<KCallable<Listener>>
    }

/**
 * Finds all [Listener] fields inside this object's class.
 *
 * @return An [Array] of [Listener] fields inside this object.
 */
internal val Any.listeners: Array<Listener>
    get() {
        return this::class.listeners.let { sequence ->
            val array = arrayOfNulls<Listener>(sequence.count())

            sequence.forEachIndexed { index, listener ->
                array[index] = listener.handleCall(this)
            }

            array as Array<Listener>
        }
    }